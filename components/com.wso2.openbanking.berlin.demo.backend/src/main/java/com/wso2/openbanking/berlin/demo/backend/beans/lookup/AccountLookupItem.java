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

import java.util.List;
import javax.validation.Valid;

/**
 * AccountLookupItem class
 */
public class AccountLookupItem {

    @Valid
    private Account identification = null;

    @Valid
    private List<UserIdentifier> users = null;

    /**
     * Get identification
     *
     * @return identification
     **/
    @JsonProperty("identification")
    public Account getIdentification() {
        return identification;
    }

    public void setIdentification(Account identification) {
        this.identification = identification;
    }

    public AccountLookupItem identification(Account identification) {
        this.identification = identification;
        return this;
    }

    /**
     * Get users
     *
     * @return users
     **/
    @JsonProperty("users")
    public List<UserIdentifier> getUsers() {
        return users;
    }

    public void setUsers(List<UserIdentifier> users) {
        this.users = users;
    }

    public AccountLookupItem users(List<UserIdentifier> users) {
        this.users = users;
        return this;
    }

    public AccountLookupItem addUsersItem(UserIdentifier usersItem) {
        this.users.add(usersItem);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountLookupItem {\n");

        sb.append("    identification: ").append(toIndentedString(identification)).append("\n");
        sb.append("    users: ").append(toIndentedString(users)).append("\n");
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

