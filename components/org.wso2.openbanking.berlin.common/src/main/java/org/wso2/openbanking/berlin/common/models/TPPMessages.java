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

package org.wso2.openbanking.berlin.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wso2.openbanking.accelerator.common.util.Generated;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * TPPMessages class for maintaining multiple errors.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TPPMessages {

    @NotNull
    private List<TPPMessage> tppMessages = new ArrayList<>();

    @JsonProperty("tppMessages")
    public List<TPPMessage> getTppMessages() {
        return tppMessages;
    }
    public void setTppMessages(List<TPPMessage> tppMessages) {
        this.tppMessages = tppMessages;
    }



    @Generated(message = "Excluded from code coverage since no logic is involved")
    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();
        sb.append("class TPPMessages {\n");

        sb.append("  tppMessages: ").append(tppMessages).append("\n");
        sb.append("}\n");
        return sb.toString();
    }
}
