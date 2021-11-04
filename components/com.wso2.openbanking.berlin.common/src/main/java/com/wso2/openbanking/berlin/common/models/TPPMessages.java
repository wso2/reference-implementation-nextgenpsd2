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

package com.wso2.openbanking.berlin.common.models;

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
