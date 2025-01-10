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

package org.wso2.openbanking.berlin.identity.sp.metadata;

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
