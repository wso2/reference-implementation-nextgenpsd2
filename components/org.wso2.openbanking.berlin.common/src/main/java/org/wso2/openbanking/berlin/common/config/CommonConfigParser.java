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

package org.wso2.openbanking.berlin.common.config;

import com.wso2.openbanking.accelerator.common.config.OpenBankingConfigParser;
import com.wso2.openbanking.accelerator.common.constant.OpenBankingConstants;
import com.wso2.openbanking.accelerator.common.exception.OpenBankingRuntimeException;
import com.wso2.openbanking.accelerator.common.util.CarbonUtils;
import com.wso2.openbanking.accelerator.common.util.Generated;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.common.constants.CommonConstants;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.enums.ConsentTypeEnum;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * Config parser for Berlin toolkit.
 */
public class CommonConfigParser {

    // To enable attempted thread-safety using double-check locking
    private static final Object lock = new Object();
    private static final Log log = LogFactory.getLog(CommonConfigParser.class);

    private static volatile CommonConfigParser parser;
    private static String configFilePath;
    private SecretResolver secretResolver;
    private OMElement rootElement;

    private static final Map<String, Object> configuration = new HashMap<>();
    private static final Map<String, String> consentMgtConfigs = new HashMap<>();

    /**
     * Private Constructor of config parser.
     */
    private CommonConfigParser() {

        buildConfiguration();
    }

    /**
     * Singleton getInstance method to create only one object.
     *
     * @return OpenBankingConfigParser object
     */
    public static CommonConfigParser getInstance() {

        if (parser == null) {
            synchronized (lock) {
                if (parser == null) {
                    parser = new CommonConfigParser();
                }
            }
        }
        return parser;
    }

    /**
     * Method to get an instance of ConfigParser when custom file path is provided.
     *
     * @param filePath Custom file path
     * @return OpenBankingConfigParser object
     */
    public static CommonConfigParser getInstance(String filePath) {

        configFilePath = filePath;
        return getInstance();
    }

    /**
     * Method to read the configuration as a model and put them in the configuration map.
     */
    private void buildConfiguration() {

        InputStream inStream = null;

        try {
            if (configFilePath != null) {
                File openBankingConfigXml = new File(configFilePath);
                if (openBankingConfigXml.exists()) {
                    inStream = new FileInputStream(openBankingConfigXml);
                }
            } else {
                File openBankingConfigXml = new File(CarbonUtils.getCarbonConfigDirPath(),
                        CommonConstants.OB_CONFIG_FILE);
                if (openBankingConfigXml.exists()) {
                    inStream = new FileInputStream(openBankingConfigXml);
                }
            }
            if (inStream == null) {
                String message = ErrorConstants.CONFIG_NOT_FOUND + configFilePath;
                log.error(message);
                throw new FileNotFoundException(message);
            }
            StAXOMBuilder builder = new StAXOMBuilder(inStream);
            builder.setDoDebug(false);
            rootElement = builder.getDocumentElement();
            Stack<String> nameStack = new Stack<>();
            secretResolver = SecretResolverFactory.create(rootElement, true);
            readChildElements(rootElement, nameStack);
            buildConsentManagementConfigs();
        } catch (IOException | XMLStreamException | OMException e) {
            throw new OpenBankingRuntimeException(ErrorConstants.CONFIGURATION_BUILD_ERROR,
                    e);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.error(ErrorConstants.CONFIG_INPUT_STREAM_ERROR, e);
            }
        }
    }

    /**
     * Method to obtain map of configs.
     *
     * @return Config map
     */
    public Map<String, Object> getConfiguration() {

        return configuration;
    }

    /**
     * Method to obtain map of consent management configs.
     *
     * @return Config map
     */
    public Map<String, String> getConsentMgtConfigs() {

        return consentMgtConfigs;
    }

    /**
     * Method to read text configs from xml recursively when root element is given.
     *
     * @param serverConfig XML root element object
     * @param nameStack    stack of config names
     */
    private void readChildElements(OMElement serverConfig, Stack<String> nameStack) {

        for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            nameStack.push(element.getLocalName());
            if (elementHasText(element)) {
                String key = getKey(nameStack);
                Object currentObject = configuration.get(key);
                String value = replaceSystemProperty(element.getText());
                if (secretResolver != null && secretResolver.isInitialized() &&
                        secretResolver.isTokenProtected(key)) {
                    value = secretResolver.resolve(key);
                }
                if (currentObject == null) {
                    configuration.put(key, value);
                } else if (currentObject instanceof ArrayList) {
                    ArrayList list = (ArrayList) currentObject;
                    if (!list.contains(value)) {
                        list.add(value);
                        configuration.put(key, list);
                    }
                } else {
                    if (!value.equals(currentObject)) {
                        ArrayList<Object> arrayList = new ArrayList<>(2);
                        arrayList.add(currentObject);
                        arrayList.add(value);
                        configuration.put(key, arrayList);
                    }
                }
            }
            readChildElements(element, nameStack);
            nameStack.pop();
        }
    }

    /**
     * Method to check whether config element has text value.
     *
     * @param element root element as a object
     * @return availability of text in the config
     */
    private boolean elementHasText(OMElement element) {

        String text = element.getText();
        return text != null && text.trim().length() != 0;
    }

    /**
     * Method to obtain config key from stack.
     *
     * @param nameStack Stack of strings with names
     * @return key as a String
     */
    private String getKey(Stack<String> nameStack) {

        StringBuilder key = new StringBuilder();
        for (int index = 0; index < nameStack.size(); index++) {
            String name = nameStack.elementAt(index);
            key.append(name).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));
        return key.toString();
    }

    /**
     * Method to replace system properties in configs.
     *
     * @param text String that may require modification
     * @return modified string
     */
    private String replaceSystemProperty(String text) {

        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        StringBuilder textBuilder = new StringBuilder(text);
        while (indexOfStartingChars < textBuilder.indexOf("${")
                && (indexOfStartingChars = textBuilder.indexOf("${")) != -1
                && (indexOfClosingBrace = textBuilder.indexOf("}")) != -1) { // Is a property used?
            String sysProp = textBuilder.substring(indexOfStartingChars + 2, indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                textBuilder = new StringBuilder(textBuilder.substring(0, indexOfStartingChars) + propValue
                        + textBuilder.substring(indexOfClosingBrace + 1));
            }
            if (sysProp.equals(OpenBankingConstants.CARBON_HOME) &&
                    System.getProperty(OpenBankingConstants.CARBON_HOME).equals(".")) {
                textBuilder.insert(0, new File(".").getAbsolutePath() + File.separator);
            }
        }
        return textBuilder.toString();
    }

    private void buildConsentManagementConfigs() {

        OMElement consentMgtElement = rootElement.getFirstChildWithName(
                new QName(CommonConstants.OB_BERLIN_CONFIG_QNAME, CommonConstants.CONSENT_MGT_CONFIG_TAG));

        if (consentMgtElement != null) {
            //obtaining each parameter type element under ConsentManagement tag
            Iterator parameterTypeElement = consentMgtElement.getChildElements();
            while (parameterTypeElement.hasNext()) {
                OMElement parameterType = (OMElement) parameterTypeElement.next();
                String parameterTypeName = parameterType.getLocalName();
                String parameterValues = parameterType.getText();

                consentMgtConfigs.put(parameterTypeName, parameterValues);
            }
        }

    }

    /**
     * Returns the element with the provided local part.
     *
     * @param localPart local part name
     * @return Corresponding OMElement
     */
    public OMElement getConfigElement(String localPart) {

        return rootElement.getFirstChildWithName(new QName(CommonConstants.OB_BERLIN_CONFIG_QNAME, localPart));
    }

    /**
     * Returns the list of configured SCA methods.
     *
     * @return List of Maps of SCA method configurations.
     */
    public List<Map<String, String>> getSupportedScaMethods() {

        List<Map<String, String>> supportedScaMethodsList = new ArrayList<>();

        Iterator iterator = getConfigElement(CommonConstants.CONSENT_MGT_CONFIG_TAG)
                .getFirstChildWithName(new QName(CommonConstants.OB_BERLIN_CONFIG_QNAME,
                        CommonConstants.SCA_CONFIG_TAG))
                .getFirstChildWithName(new QName(CommonConstants.OB_BERLIN_CONFIG_QNAME,
                        CommonConstants.SUPPORTED_SCA_METHODS_CONFIG_TAG))
                .getChildElements();

        while (iterator.hasNext()) {
            Map<String, String> singleMethod = new HashMap<>();
            OMElement element = (OMElement) iterator.next();
            Iterator subElements = element.getChildElements();

            while (subElements.hasNext()) {
                OMElement subElement = (OMElement) subElements.next();
                singleMethod.put(subElement.getLocalName(), subElement.getText());
            }
            supportedScaMethodsList.add(singleMethod);
        }

        return supportedScaMethodsList;
    }

    /**
     * Returns the list of configured SCA approaches.
     *
     * @return List of Maps of SCA approach configurations.
     */
    public List<Map<String, String>> getSupportedScaApproaches() {

        List<Map<String, String>> supportedScaApproachesList = new ArrayList<>();

        Iterator iterator = getConfigElement(CommonConstants.CONSENT_MGT_CONFIG_TAG)
                .getFirstChildWithName(new QName(CommonConstants.OB_BERLIN_CONFIG_QNAME,
                        CommonConstants.SCA_CONFIG_TAG))
                .getFirstChildWithName(new QName(CommonConstants.OB_BERLIN_CONFIG_QNAME,
                        CommonConstants.SUPPORTED_SCA_APPROACHES_CONFIG_TAG))
                .getChildElements();

        while (iterator.hasNext()) {
            Map<String, String> singleMethod = new HashMap<>();
            OMElement element = (OMElement) iterator.next();
            Iterator subElements = element.getChildElements();

            while (subElements.hasNext()) {
                OMElement subElement = (OMElement) subElements.next();
                singleMethod.put(subElement.getLocalName(), subElement.getText());
            }
            supportedScaApproachesList.add(singleMethod);
        }

        return supportedScaApproachesList;
    }

    public boolean isScaRequired() {
        return Boolean.parseBoolean((String) getConfiguration()
                .get(CommonConstants.SCA_REQUIRED));
    }

    public String getOauthMetadataEndpoint() {
        return (String) getConfiguration().get(CommonConstants.OAUTH_METADATA_ENDPOINT);
    }

    public boolean isFrequencyPerDayThrottlingEnabled() {
        return Boolean.parseBoolean((String) getConfiguration().get(CommonConstants.FREQ_PER_DAY_ENABLED));
    }

    public int getConfiguredMinimumFreqPerDay() {
        return getConfiguration().get(CommonConstants.FREQ_PER_DAY_CONFIG_VALUE) == null ? 4 :
                Integer.parseInt((String) getConfiguration().get(CommonConstants.FREQ_PER_DAY_CONFIG_VALUE));
    }

    public boolean isValidUntilDateCapEnabled() {
        return getConfiguration().get(CommonConstants.VALID_UNTIL_DATE_CAP_ENABLED) != null
                && Boolean.parseBoolean((String) getConfiguration().get(CommonConstants.VALID_UNTIL_DATE_CAP_ENABLED));
    }

    public int validUntilDaysCap() {
        return Integer.parseInt(getConfiguration().get(CommonConstants.VALID_UNTIL_DAYS).toString());
    }

    public String getApiVersion(String consentType) {
        if (ConsentTypeEnum.ACCOUNTS.toString().equals(consentType)) {
            return getConfiguration().get(CommonConstants.AIS_API_VERSION).toString();
        }

        if (ConsentTypeEnum.PAYMENTS.toString().equals(consentType)
                || ConsentTypeEnum.BULK_PAYMENTS.toString().equals(consentType)
                || ConsentTypeEnum.PERIODIC_PAYMENTS.toString().equals(consentType)) {
            return getConfiguration().get(CommonConstants.PIS_API_VERSION).toString();
        }

        if (ConsentTypeEnum.FUNDS_CONFIRMATION.toString().equals(consentType)) {
            return getConfiguration().get(CommonConstants.PIIS_API_VERSION).toString();
        }

        return "";
    }

    public String getMaxFuturePaymentDays() {
        return (getConfiguration().get(CommonConstants.MAX_FUTURE_PAYMENT_DAYS) == null) ? "" :
                (String) getConfiguration().get(CommonConstants.MAX_FUTURE_PAYMENT_DAYS);
    }

    public List<String> getSupportedAccountReferenceTypes() {

        Object supportedAccountReferenceTypes =
                getConfiguration().get(CommonConstants.SUPPORTED_ACCOUNT_REFERENCE_TYPES_PATH);
        List<String> accountReferenceTypes = new ArrayList<>();
        if (supportedAccountReferenceTypes instanceof ArrayList) {
            accountReferenceTypes.addAll((ArrayList) supportedAccountReferenceTypes);
        } else if (supportedAccountReferenceTypes instanceof String) {
            accountReferenceTypes.add((String) supportedAccountReferenceTypes);
        }
        return accountReferenceTypes;
    }

    /**
     * Get supported code challenge methods for Berlin authorization request.
     *
     * @return List of supported code challenge methods
     */
    public List<String> getSupportedCodeChallengeMethods() {

        Object supportedCodeChallengeMethods =
                getConfiguration().get(CommonConstants.SUPPORTED_CODE_CHALLENGE_METHODS);
        List<String> codeChallengeMethods = new ArrayList<>();
        if (supportedCodeChallengeMethods instanceof ArrayList) {
            codeChallengeMethods.addAll((ArrayList) supportedCodeChallengeMethods);
        } else if (supportedCodeChallengeMethods instanceof String) {
            codeChallengeMethods.add((String) supportedCodeChallengeMethods);
        }
        return codeChallengeMethods;
    }

    public boolean isAccountIdValidationEnabled() {
        return Boolean.parseBoolean((String) getConfiguration().get(CommonConstants.IS_ACCOUNT_ID_VALIDATION_ENABLED));
    }


    public boolean isAccountIdValidationEnabledForCofConsent() {
        return Boolean.parseBoolean((String) getConfiguration()
                .get(CommonConstants.IS_ACCOUNT_ID_VALIDATION_ENABLED_FOR_COF_CONSENT));
    }

    public String getShareableAccountsRetrieveEndpoint() {
        if (getConfiguration().get(CommonConstants.SHAREABLE_ACCOUNTS_RETRIEVAL_ENDPOINT) == null) {
            log.error("Shareable accounts retrieve endpoint is not set");
            return "";
        } else {
            return ((String) getConfiguration().get(CommonConstants.SHAREABLE_ACCOUNTS_RETRIEVAL_ENDPOINT)).trim();
        }
    }

    public String getPayableAccountsRetrieveEndpoint() {
        if (getConfiguration().get(CommonConstants.PAYABLE_ACCOUNTS_RETRIEVAL_ENDPOINT) == null) {
            log.error("Payable accounts retrieve endpoint is not set");
            return "";
        } else {
            return ((String) getConfiguration().get(CommonConstants.PAYABLE_ACCOUNTS_RETRIEVAL_ENDPOINT)).trim();
        }
    }

    /**
     * Returns the configured payments backend URL.
     *
     * @return the payment backend URL.
     */
    public String getPaymentsBackendURL() {

        return ((String) getConfiguration().get(CommonConstants.PAYMENTS_BACKEND_URL)).trim();
    }

    public boolean isMultipleRecurringConsentEnabled() {
        return Boolean.parseBoolean((String) getConfiguration()
                .get(CommonConstants.MULTIPLE_RECURRING_CONSENT_ENABLED));
    }

    public List<String> getSupportedHashAlgorithms() {

        Object supportedHashAlgorithms = getConfiguration().get(CommonConstants.SUPPORTED_HASH_ALGORITHMS);
        List<String> supportedHashAlgorithmsList = new ArrayList<>();
        if (supportedHashAlgorithms instanceof ArrayList) {
            supportedHashAlgorithmsList.addAll((ArrayList) supportedHashAlgorithms);
        } else if (supportedHashAlgorithms instanceof String) {
            supportedHashAlgorithmsList.add((String) supportedHashAlgorithms);
        }
        return supportedHashAlgorithmsList;
    }

    public List<String> getSupportedSignatureAlgorithms() {

        Object supportedSignatureAlgorithms = getConfiguration().get(CommonConstants.SUPPORTED_SIGNATURE_ALGORITHMS);
        List<String> supportedSignatureAlgorithmsList = new ArrayList<>();
        if (supportedSignatureAlgorithms instanceof ArrayList) {
            supportedSignatureAlgorithmsList.addAll((ArrayList) supportedSignatureAlgorithms);
        } else if (supportedSignatureAlgorithms instanceof String) {
            supportedSignatureAlgorithmsList.add((String) supportedSignatureAlgorithms);
        }
        return supportedSignatureAlgorithmsList;
    }

    @Generated(message = "Skipped from tests because this method reads config from open-banking.xml")
    public boolean isPsd2RoleValidationEnabled() {

        Object psd2RoleValidationEnabledObj = OpenBankingConfigParser.getInstance()
                .getConfigElementFromKey(OpenBankingConstants.PSD2_ROLE_VALIDATION_ENABLED);
        return Boolean.parseBoolean((String) psd2RoleValidationEnabledObj);
    }

    public String getOrgIdValidationRegex() {

        return ((String) getConfiguration().get(CommonConstants.ORG_ID_VALIDATION_REGEX)).trim();

    }

    public List<String> getRevocationValidationExcludedIssuers() {

        Object excludedIssuers = OpenBankingConfigParser.getInstance()
                .getConfigElementFromKey(OpenBankingConstants.CERTIFICATE_REVOCATION_VALIDATION_EXCLUDED_ISSUERS);
        List<String> revocationValidationExcludedIssuers = new ArrayList<>();
        if (excludedIssuers instanceof String) {
            revocationValidationExcludedIssuers.add((String) excludedIssuers);
        } else if (excludedIssuers instanceof ArrayList) {
            revocationValidationExcludedIssuers.addAll((ArrayList) excludedIssuers);
        }
        return revocationValidationExcludedIssuers;
    }

    public boolean isPaymentDebtorAccountCurrencyValidationEnabled() {

        return Boolean.parseBoolean((String) getConfiguration()
                .get(CommonConstants.IS_PAYMENT_DEBTOR_ACCOUNT_CURRENCY_VALIDATION_ENABLED));
    }
}
