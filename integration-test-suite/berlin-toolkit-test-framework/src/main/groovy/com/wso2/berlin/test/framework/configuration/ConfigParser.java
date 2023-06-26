/*

Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
This software is the property of WSO2 LLC. and its suppliers, if any.
Dissemination of any information or reproduction of any material contained
herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
You may not alter or remove any copyright or other notice from copies of this content.
*/

package com.wso2.berlin.test.framework.configuration;

import com.wso2.bfsi.test.framework.exception.TestFrameworkException;
import com.wso2.openbanking.test.framework.configuration.OBConfigurationService;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;


/**
 * Configuration parser to read configurations from test-config.xml
 */
public class ConfigParser {

    private static final Object lock = new Object();
    private static Log log = LogFactory.getLog(ConfigParser.class);
    static String configFilePath;
    private static Map<String, Object> configuration = new HashMap<>();
    private static Map<String, String> consentProcessorConfig = new HashMap<>();
    private static volatile ConfigParser parser = null;
    private OMElement rootElement;

    protected ConfigParser() throws TestFrameworkException {

        buildGlobalConfiguration();
    }

    /**
     * Maintain single instance of Config parser through out the implementations.
     *
     * @return ConfigParser object
     */
    public static ConfigParser getInstance() {

        if (parser == null) {
            synchronized (lock) {
                if (parser == null) {
                    try {
                        parser = new ConfigParser();
                    } catch (TestFrameworkException e) {
                        log.error("Failed to initiate config parser", e);
                        parser = null;
                    }
                }
            }
        }
        return parser;
    }

    public Map<String, Object> getConfiguration() {

        return configuration;
    }

    /**
     * Build global configurations from test-config.xml.
     */
    void buildGlobalConfiguration() throws TestFrameworkException {

        InputStream inStream = null;
        StAXOMBuilder builder;

        String warningMessage = "";
        try {
            if (configFilePath != null) {
                File openBankingConfig = new File(configFilePath);
                if (openBankingConfig.exists()) {
                    inStream = new FileInputStream(openBankingConfig);
                } else {
                    log.warn("No file found in the specified path: " + configFilePath
                            + ". Proceeding with default location.");
                }
            } else {
                File configXML = new File(this.getClass().getClassLoader()
                        .getResource("TestConfiguration.xml").getFile());
                if (configXML.exists()) {
                    inStream = new FileInputStream(configXML);
                }
            }

            if (inStream == null) {
                String message = "Test Framework configuration not found. Cause - " + warningMessage;
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                throw new FileNotFoundException(message);
            }

            builder = new StAXOMBuilder(inStream);
            rootElement = builder.getDocumentElement();
            Deque<String> elementNames = new ArrayDeque<>();
            readChildElements(rootElement, elementNames);

        } catch (IOException | XMLStreamException e) {
            throw new TestFrameworkException("Error occurred while building configuration from test-config.xml", e);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing the input stream for test-config.xml", e);
            }
        }
    }

    /**
     * Read element recursively and put in the configuration map.
     *
     * @param serverConfig OM Element
     * @param elementNames Deque of element names
     */
    private void readChildElements(OMElement serverConfig, Deque<String> elementNames) {

        for (Iterator childElements = serverConfig.getChildElements(); childElements.hasNext(); ) {
            OMElement element = (OMElement) childElements.next();
            elementNames.push(element.getLocalName());
            if (elementHasText(element)) {
                String key = getKey(elementNames);
                Object currentObject = configuration.get(key);
                String value = replaceSystemProperty(element.getText());
                if (currentObject == null) {
                    configuration.put(key, value);
                } else if (currentObject instanceof ArrayList) {
                    List<String> list = (ArrayList) currentObject;
                    if (!list.contains(value)) {
                        list.add(value);
                        configuration.put(key, list);
                    }
                } else {
                    if (!value.equals(currentObject)) {
                        List<Object> arrayList = new ArrayList<>(2);
                        arrayList.add(currentObject);
                        arrayList.add(value);
                        configuration.put(key, arrayList);
                    }
                }
            }
            readChildElements(element, elementNames);
            elementNames.pop();
        }
    }

    private boolean elementHasText(OMElement element) {

        String text = element.getText();
        return text != null && text.trim().length() != 0;
    }

    /**
     * Converts the hierarchical element name to key.
     *
     * @param elementNames hierarchical element name
     * @return key name
     */
    private String getKey(Deque<String> elementNames) {

        StringBuilder key = new StringBuilder();
        for (Iterator itr = elementNames.descendingIterator(); itr.hasNext(); ) {
            key.append(itr.next()).append(".");
        }
        key.deleteCharAt(key.lastIndexOf("."));
        return key.toString();
    }

    private String replaceSystemProperty(String propertyName) {

        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        StringBuilder nameBuilder = new StringBuilder(propertyName);
        while (indexOfStartingChars < nameBuilder.indexOf("${")
                && (indexOfStartingChars = nameBuilder.indexOf("${")) != -1
                && (indexOfClosingBrace = nameBuilder.toString().indexOf('}')) != -1) { // Is a property used?
            String sysProp = nameBuilder.substring(indexOfStartingChars + 2, indexOfClosingBrace);
            String propValue = System.getProperty(sysProp);
            if (propValue != null) {
                nameBuilder = new StringBuilder(nameBuilder.substring(0, indexOfStartingChars) + propValue +
                        nameBuilder.substring(indexOfClosingBrace + 1));
            }
        }
        propertyName = nameBuilder.toString();
        return propertyName;
    }


}

