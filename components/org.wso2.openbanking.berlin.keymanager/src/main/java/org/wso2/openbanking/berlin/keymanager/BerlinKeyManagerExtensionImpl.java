/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.openbanking.berlin.keymanager;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingException;
import com.wso2.openbanking.accelerator.common.util.CertificateUtils;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContent;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContentExtractor;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.accelerator.keymanager.OBKeyManagerExtensionInterface;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.openbanking.berlin.common.config.CommonConfigParser;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Berlin specific validation for app creation from dev portal.
 */
public class BerlinKeyManagerExtensionImpl implements OBKeyManagerExtensionInterface {

    private static final Log log = LogFactory.getLog(BerlinKeyManagerExtensionImpl.class);

    /**
     * Validate additional properties.
     *
     * @param obAdditionalProperties OB Additional Properties Map
     * @throws APIManagementException when failed to validate a given property
     */
    public void validateAdditionalProperties(Map<String, ConfigurationDto> obAdditionalProperties)
            throws APIManagementException {

        String regulatory = getValueForAdditionalProperty(obAdditionalProperties, OpenBankingConstants.REGULATORY);
        if ("true".equals(regulatory) || "false".equals(regulatory)) {
            if (Boolean.parseBoolean(regulatory)) {
                String spCertificate = getValueForAdditionalProperty(obAdditionalProperties,
                        BerlinKeyManagerConstants.SP_CERTIFICATE);
                String orgId = getValueForAdditionalProperty(obAdditionalProperties, BerlinKeyManagerConstants.ORG_ID);
                validateOrganizationIdPattern(orgId);
                validateCertificate(spCertificate, orgId);
            }
        } else {
            String msg = "Invalid value for regulatory property";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }

    }

    /**
     * Obtain the value from Configuration DTO object.
     * @param obAdditionalProperties Additional Property Map
     * @param propertyName Property Name
     * @return value for given property
     * @throws APIManagementException
     */
    protected String getValueForAdditionalProperty(Map<String, ConfigurationDto> obAdditionalProperties,
                                               String propertyName) throws APIManagementException {
        ConfigurationDto property = obAdditionalProperties.get(propertyName);
        if (property != null) {
            List<Object> values = property.getValues();
            if (values.size() > 0) {
                return (String) values.get(0);
            } else {
                String msg = "No value found for additional property: " + propertyName;
                log.error(msg);
                throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
            }
        } else {
            String msg = propertyName + " property not found in additional properties";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
    }

    /**
     * Validate certificate provided as user input.
     * @param cert Certificate string
     * @param organizationId Organization ID
     * @throws APIManagementException
     */
    @Generated(message = "Excluding from code coverage since it is covered from other method")
    protected void validateCertificate(String cert, String organizationId) throws APIManagementException {
        X509Certificate certificate;
        try {
            certificate = CertificateUtils.parseCertificate(cert);
        } catch (OpenBankingException e) {
            String msg = "Certificate unavailable";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
        if (CertificateValidationUtils.isExpired(certificate)) {
            String msg = "Provided certificate expired";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
        if (!validateOrganizationIdFromCert(certificate, organizationId)) {
            String msg = "Provided organization ID is not equal to organization ID in the " +
                    "certificate";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
        if (!validateRolesFromCert(certificate)) {
            String msg = "Certificate does not have necessary permissions";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
        log.debug("Provided certificate successfully validated");
    }

    /**
     * Validate roles in the certificate.
     *
     * @param certificate X509Certificate
     * @return isValid
     * @throws APIManagementException
     */

    protected boolean validateRolesFromCert(X509Certificate certificate) throws APIManagementException {

        CertificateContent certificateContent = extractCertificateContent(certificate);
        Set<String> allowedRoles = new HashSet<>();
        if (CommonConfigParser.getInstance().isPsd2RoleValidationEnabled()) {
            Map<String, List<String>> definedScopes = OpenBankingConfigParser.getInstance().getAllowedScopes();
            for (Map.Entry<String, List<String>> scope : definedScopes.entrySet()) {
                allowedRoles.addAll(scope.getValue());
            }
            List<String> providedRoles = certificateContent.getPspRoles();
            return allowedRoles.containsAll(providedRoles);
        } else {
            // Skip role validation if the role validation configuration is set to false
            if (log.isDebugEnabled()) {
                log.debug("Skipping role validation as it is not enabled from the configuration");
            }
            return true;
        }

    }

    /**
     * Validate the organization ID in certificate against provided organization Id.
     * @param certificate X509Certificate
     * @param organizationId organization ID
     * @return isValid
     * @throws APIManagementException
     */
    protected boolean validateOrganizationIdFromCert(X509Certificate certificate, String organizationId)
            throws APIManagementException {

        CertificateContent content = extractCertificateContent(certificate);
        String orgIdFromCert = content.getPspAuthorisationNumber();
        return organizationId.equalsIgnoreCase(orgIdFromCert);
    }

    /**
     * Validate organization ID pattern.
     * @param organizationId organization ID
     * @throws APIManagementException
     */
    protected void validateOrganizationIdPattern(String organizationId) throws APIManagementException {

        Pattern regexPattern = Pattern.compile(getConfigParser().getOrgIdValidationRegex());

        Matcher matcher = regexPattern.matcher(organizationId);
        if (!matcher.find()) {
            String msg = "Organization ID is invalid";
            log.error(msg);
            throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
        if (log.isDebugEnabled()) {
            log.debug("Organization ID passed the regex validation");
        }
    }

    @Generated(message = "Excluding from code coverage since it is covered from other methods")
    protected CertificateContent extractCertificateContent(X509Certificate certificate) throws
    APIManagementException {
        try {
            return CertificateContentExtractor.extract(certificate);
        } catch (CertificateValidationException e) {
            String msg = "Error in parsing the provided certificate";
            log.error(msg);
            throw new APIManagementException(msg, e, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
        }
    }

    public void doPreUpdateSpApp(OAuthConsumerAppDTO oAuthConsumerAppDTO, ServiceProvider serviceProvider,
                                 HashMap<String, String> additionalProperties, boolean isCreateApp)
            throws APIManagementException {

        // This method is called both at app creation and app update
        // Enable PKCE and add SP certificate to the service provider at only app creation for regulatory apps
        if (isCreateApp && Boolean.parseBoolean(additionalProperties.get(OpenBankingConstants.REGULATORY))) {
            String appName = oAuthConsumerAppDTO.getApplicationName();
            oAuthConsumerAppDTO.setPkceMandatory(true);
            oAuthConsumerAppDTO.setPkceSupportPlain(true);
            if (log.isDebugEnabled()) {
                log.debug("PKCE enabled for application: " + appName);
            }
            String certificate = additionalProperties.get(BerlinKeyManagerConstants.SP_CERTIFICATE);
            if (certificate != null) {
                serviceProvider.setCertificateContent(certificate);
            } else {
                String errMsg = "Certificate not available in the request";
                throw new APIManagementException(errMsg, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
            }
        }

    }

    /**
     * Do changes to app request before creating the app at toolkit level.
     *
     * @param additionalProperties Values for additional property list defined in the config
     * @throws APIManagementException when failed to validate a given property
     */
    @Generated(message = "Excluding from code coverage since it is covered from other methods")
    public void doPreCreateApplication(OAuthAppRequest oAuthAppRequest, HashMap<String, String> additionalProperties)
            throws APIManagementException {

        // Set Organization ID as the consumer key for regulatory apps
        if (Boolean.parseBoolean(additionalProperties.get(OpenBankingConstants.REGULATORY))) {
            setOrgIdAsClientID(oAuthAppRequest, additionalProperties);
        }

    }

    /**
     * Do changes to app request before updating the app at toolkit level.
     *
     * @param additionalProperties Values for additional property list defined in the config
     * @throws APIManagementException when failed to validate a given property
     */
    public void doPreUpdateApplication(OAuthAppRequest oAuthAppRequest, HashMap<String, String> additionalProperties,
                                       ServiceProvider serviceProvider)
            throws APIManagementException {

        ServiceProviderProperty[] spProperties = serviceProvider.getSpProperties();
        validateDbPropertyChange(spProperties, additionalProperties, OpenBankingConstants.REGULATORY);

        // If application is regulatory, check if certificate or organization id is changed in the update
        if (Boolean.parseBoolean(additionalProperties.get(OpenBankingConstants.REGULATORY))) {
            validateDbPropertyChange(spProperties, additionalProperties, BerlinKeyManagerConstants.SP_CERTIFICATE);
            validateDbPropertyChange(spProperties, additionalProperties, BerlinKeyManagerConstants.ORG_ID);
        }


    }

    /**
     * Check if the input value for a sp property is different from its value registered in the database.
     * @param spProperties Service provider property array
     * @param propertyName Property name
     * @param additionalProperties Values for additional property list defined in the config
     * @throws APIManagementException
     */
    protected void validateDbPropertyChange(ServiceProviderProperty[] spProperties,
                                            HashMap<String, String> additionalProperties, String propertyName)
            throws APIManagementException {

        for (ServiceProviderProperty spProperty : spProperties) {
            if (StringUtils.equals(spProperty.getName(), propertyName)) {
                String inputValue = additionalProperties.get(propertyName);
                String registeredValue = spProperty.getValue();
                if (!StringUtils.equals(registeredValue, inputValue)) {
                    //throw error if input value not equal to the DB value
                    String errMsg = "Input value for property " + inputValue + " provided for service provider property"
                            + propertyName + " is different from the value in the database";
                    log.error(errMsg);
                    throw new APIManagementException(errMsg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
                }
            }
        }
    }

    /**
     * Set Organization ID as the consumer key for regulatory apps.
     * @param oAuthAppRequest
     * @param additionalProperties
     * @throws APIManagementException
     */
    protected void setOrgIdAsClientID(OAuthAppRequest oAuthAppRequest, HashMap<String, String> additionalProperties)
            throws APIManagementException {

        String orgId = additionalProperties.get(BerlinKeyManagerConstants.ORG_ID);
        if (StringUtils.isNotBlank(orgId)) {
            oAuthAppRequest.getOAuthApplicationInfo().setClientId(orgId);
        } else {
            String errMsg = "Org ID not available in the request";
            throw new APIManagementException(errMsg, ExceptionCodes.OAUTH2_APP_CREATION_FAILED);
        }
    }

    @Generated(message = "Created for testing purposes")
    protected CommonConfigParser getConfigParser() {

        return CommonConfigParser.getInstance();
    }

}
