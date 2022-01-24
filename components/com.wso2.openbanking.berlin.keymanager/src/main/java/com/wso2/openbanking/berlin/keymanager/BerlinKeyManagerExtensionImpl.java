/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses. For specific
 * language governing the permissions and limitations under this license,
 * please see the license as well as any agreement you’ve entered into with
 * WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.keymanager;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.exception.CertificateValidationException;
import com.wso2.openbanking.accelerator.common.util.Generated;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContent;
import com.wso2.openbanking.accelerator.common.util.eidas.certificate.extractor.CertificateContentExtractor;
import com.wso2.openbanking.accelerator.gateway.executor.util.CertificateValidationUtils;
import com.wso2.openbanking.accelerator.keymanager.OBKeyManagerExtensionInterface;
import com.wso2.openbanking.berlin.common.config.CommonConfigParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.OAuthAppRequest;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Berlin specific validation for app creation from dev portal
 */
public class BerlinKeyManagerExtensionImpl implements OBKeyManagerExtensionInterface {

    private static final Log log = LogFactory.getLog(BerlinKeyManagerExtensionImpl.class);

    /**
     * Validate additional properties
     *
     * @param obAdditionalProperties OB Additional Properties Map
     * @throws APIManagementException when failed to validate a given property
     */
    public void validateAdditionalProperties(Map<String, ConfigurationDto> obAdditionalProperties)
            throws APIManagementException {

        String regulatory = getValueForAdditionalProperty(obAdditionalProperties, BerlinKeyManagerConstants.REGULATORY);
        String spCertificate = getValueForAdditionalProperty(obAdditionalProperties,
                BerlinKeyManagerConstants.SP_CERTIFICATE);
        String orgId = getValueForAdditionalProperty(obAdditionalProperties, BerlinKeyManagerConstants.ORG_ID);
        if ("true".equals(regulatory) || "false".equals(regulatory)) {
            if (Boolean.parseBoolean(regulatory)) {
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
     * Obtain the value from Configuration DTO object
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
     * Validate certificate provided as user input
     * @param cert Certificate string
     * @param organizationId Organization ID
     * @throws APIManagementException
     */
    @Generated(message = "Excluding from code coverage since it is covered from other method")
    protected void validateCertificate(String cert, String organizationId) throws APIManagementException {
        X509Certificate certificate = parseTransportCert(cert);
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
     * Validate roles in the certificate
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
     * Validate the organization ID in certificate against provided organization Id
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
     * Validate organization ID pattern
     * @param organizationId organization ID
     * @throws APIManagementException
     */
    protected void validateOrganizationIdPattern(String organizationId) throws APIManagementException {

        Pattern regexPattern = Pattern.compile(CommonConfigParser.getInstance().getOrgIdValidationRegex());

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

    @Generated(message = "Excluding from code coverage since it is covered from other method")
    protected X509Certificate parseTransportCert(String spCertificate) throws APIManagementException {
        try {
            Optional<X509Certificate> certificate = CertificateValidationUtils.parseTransportCert(spCertificate);
            if (certificate.isPresent()) {
                return certificate.get();
            } else {
                String msg = "Certificate unavailable";
                log.error(msg);
                throw new APIManagementException(msg, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
            }
        } catch (CertificateValidationException e) {
            String msg = "Error in parsing the provided certificate";
            log.error(msg);
            throw new APIManagementException(msg, e, ExceptionCodes.OAUTH2_APP_UPDATE_FAILED);
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

    public void doPreUpdateSpApp(OAuthConsumerAppDTO oAuthConsumerAppDTO,
                                 ServiceProvider serviceProvider,
                                 HashMap<String, String> additionalProperties)
            throws APIManagementException {

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

    @Generated(message = "Excluding from code coverage since it is covered from other methods")
    public void doPreCreateApplication(OAuthAppRequest oAuthAppRequest, HashMap<String, String> additionalProperties)
            throws APIManagementException {

        setOrgIdAsClientID(oAuthAppRequest, additionalProperties);
    }

    public void doPreUpdateApplication(OAuthAppRequest oAuthAppRequest, HashMap<String, String> additionalProperties)
            throws APIManagementException {
        //TODO: fail application update if certificate or org_id is changed
    }


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

}
