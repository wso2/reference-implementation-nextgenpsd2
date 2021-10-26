/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 *  This software is the property of WSO2 Inc. and its suppliers, if any.
 *  Dissemination of any information or reproduction of any material contained
 *  herein is strictly forbidden, unless permitted by WSO2 in accordance with
 *  the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
 *  For specific language governing the permissions and limitations under this
 *  license, please see the license as well as any agreement you’ve entered into
 *  with WSO2 governing the purchase of this software and any associated services.
 */

package com.wso2.openbanking.berlin.common.utils;

/**
 * Common constants.
 */
public class CommonConstants {

    // Config tag constants
    public static final String OB_CONFIG_FILE = "open-banking-berlin.xml";
    public static final String OB_BERLIN_CONFIG_QNAME = "http://wso2.org/projects/carbon/open-banking-berlin.xml";
    public static final String CONSENT_MGT_CONFIG_TAG = "ConsentManagement";
    public static final String SCA_CONFIG_TAG = "SCA";
    public static final String SUPPORTED_SCA_METHODS_CONFIG_TAG = "SupportedSCAMethods";
    public static final String SUPPORTED_SCA_APPROACHES_CONFIG_TAG = "SupportedSCAApproaches";
    public static final String SCA_REQUIRED = "ConsentManagement.SCA.Required";
    public static final String OAUTH_METADATA_ENDPOINT = "ConsentManagement.SCA.OAuthMetadataEndpoint";
    public static final String FREQ_PER_DAY_CONFIG_VALUE = "ConsentManagement.FrequencyPerDay.Frequency";
    public static final String VALID_UNTIL_DATE_CAP_ENABLED = "ConsentManagement.ValidUntilDateCap.Enabled";
    public static final String VALID_UNTIL_DAYS = "ConsentManagement.ValidUntilDateCap.ValidUntilDays";
    public static final String AIS_API_VERSION = "ConsentManagement.APIVersions.AIS";
    public static final String PIS_API_VERSION = "ConsentManagement.APIVersions.PIS";
    public static final String PIIS_API_VERSION = "ConsentManagement.APIVersions.PIIS";
    public static final String SCA_TYPE = "Type";
    public static final String SCA_VERSION = "Version";
    public static final String SCA_ID = "Id";
    public static final String SCA_NAME = "Name";
    public static final String SCA_MAPPED_APPROACH = "MappedApproach";
    public static final String SCA_DESCRIPTION = "Description";
    public static final String SCA_DEFAULT = "Default";

    public static final String SCA_APPROACH_KEY = "SCA-Approach";
    public static final String SCA_METHODS_KEY = "SCA-Methods";
    public static final String SCA_METHOD_KEY = "SCA-Method";
    public static final String AIS = "AIS";
    public static final String PIS = "PIS";
    public static final String PIIS = "PIIS";
}
