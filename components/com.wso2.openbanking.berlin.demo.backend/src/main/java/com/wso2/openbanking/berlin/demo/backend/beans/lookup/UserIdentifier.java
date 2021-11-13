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

package com.wso2.openbanking.berlin.demo.backend.beans.lookup;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * UserIdentifier class
 */
public class UserIdentifier {

    private String customerIdentification;

    private String userIdentification;

    /**
     * Get customerIdentification
     *
     * @return customerIdentification
     **/
    @JsonProperty("customerIdentification")
    public String getCustomerIdentification() {
        return customerIdentification;
    }

    public void setCustomerIdentification(String customerIdentification) {
        this.customerIdentification = customerIdentification;
    }

    public UserIdentifier customerIdentification(String customerIdentification) {
        this.customerIdentification = customerIdentification;
        return this;
    }

    /**
     * Get userIdentification
     *
     * @return userIdentification
     **/
    @JsonProperty("userIdentification")
    public String getUserIdentification() {
        return userIdentification;
    }

    public void setUserIdentification(String userIdentification) {
        this.userIdentification = userIdentification;
    }

    public UserIdentifier userIdentification(String userIdentification) {
        this.userIdentification = userIdentification;
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserIdentifier {\n");

        sb.append("    customerIdentification: ").append(toIndentedString(customerIdentification)).append("\n");
        sb.append("    userIdentification: ").append(toIndentedString(userIdentification)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private static String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

