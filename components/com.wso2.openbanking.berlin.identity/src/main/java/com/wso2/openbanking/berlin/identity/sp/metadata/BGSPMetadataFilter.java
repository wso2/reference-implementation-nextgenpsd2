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

package com.wso2.openbanking.berlin.identity.sp.metadata;

import com.google.common.collect.ImmutableMap;
import com.wso2.openbanking.accelerator.identity.sp.metadata.extension.SPMetadataFilter;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Impl of the SPMetadataFilterInterface
 */
public class BGSPMetadataFilter implements SPMetadataFilter {

    @Override
    public Map<String, String> filter(Map<String, String> metadata) {

        // Ex: ("client_name", "software_client_name"), property will read as "software_client_name" from metadata
        // and put as "client_name"
        Map<String, String> propertiesVsMappingName = ImmutableMap.of(
                "software_client_name", "software_client_name",
                "software_id", "software_id",
                "logo_uri", "logo_uri",
                "organization_id", "organization_id",
                "DisplayName", "DisplayName"
        );

        return propertiesVsMappingName.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> metadata.getOrDefault(e.getValue(), "")
                ));
    }
}
