<%--
 ~ Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 ~
 ~ WSO2 LLC. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied. See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
--%>

<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>

<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="includes/berlin_consent_top.jsp"/>
<%
    session.setAttribute("configParamsMap", request.getAttribute("data_requested"));
    Map<String, List<String>> consentData = (Map<String, List<String>>) request.getAttribute("data_requested");

    Map<String, List<String>> staticBulkMap = null;
    Map<String, List<String>> selectBalanceMap = null;
    Map<String, List<String>> staticBalanceMap = null;
    Map<String, List<String>> selectAccountMap = null;
    Map<String, List<String>> staticAccountMap = null;
    Map<String, List<String>> selectTransactionMap = null;
    Map<String, List<String>> staticTransactionMap = null;

    boolean isStaticBulk = false;
    boolean isSelectBalance = false;
    boolean isStaticBalance = false;
    boolean isSelectAccount = false;
    boolean isStaticAccount = false;
    boolean isSelectTransaction = false;
    boolean isStaticTransaction = false;

    if(request.getAttribute("consent_type").equals("accounts")) {
        staticBulkMap = (Map<String, List<String>>) request.getAttribute("static-bulk");
        selectBalanceMap = (Map<String, List<String>>) request.getAttribute("select-balance");
        staticBalanceMap = (Map<String, List<String>>) request.getAttribute("static-balance");
        selectAccountMap = (Map<String, List<String>>) request.getAttribute("select-account");
        staticAccountMap = (Map<String, List<String>>) request.getAttribute("static-account");
        selectTransactionMap = (Map<String, List<String>>) request.getAttribute("select-transaction");
        staticTransactionMap = (Map<String, List<String>>) request.getAttribute("static-transaction");

        isStaticBulk = Boolean.parseBoolean(request.getAttribute("isStaticBulk").toString());
        isSelectBalance = Boolean.parseBoolean(request.getAttribute("isSelectBalance").toString());
        isStaticBalance = Boolean.parseBoolean(request.getAttribute("isStaticBalance").toString());
        isSelectAccount = Boolean.parseBoolean(request.getAttribute("isSelectAccount").toString());
        isStaticAccount = Boolean.parseBoolean(request.getAttribute("isStaticAccount").toString());
        isSelectTransaction = Boolean.parseBoolean(request.getAttribute("isSelectTransaction").toString());
        isStaticTransaction = Boolean.parseBoolean(request.getAttribute("isStaticTransaction").toString());
    }
%>
<div class="row data-container">
    <div class="clearfix"></div>
    <form action="${pageContext.request.contextPath}/oauth2_authz_confirm.do" method="post" id="oauth2_authz_confirm"
          name="oauth2_authz_confirm" class="form-horizontal">
        <div class="login-form">
            <div class="form-group ui form">
                <div class="col-md-12 ui box">
                    <h3 class="ui header">

                    <%-- Change heading based on the consent type --%>
                    <c:choose>
                        <c:when test="${consent_type eq 'accounts'}">
                            <strong>${app}</strong> requests account details on your account.
                        </c:when>
                        <c:when test="${consent_type eq 'funds-confirmations'}">
                            <strong>${app}</strong> requests access to confirm the availability of funds in your account.
                        </c:when>
                        <c:when test="${consent_type eq 'payments'}">
                            <strong>${app}</strong> requests consent to do a single payment transaction ${intentSubText}
                        </c:when>
                        <c:when test="${consent_type eq 'bulk-payments'}">
                            <c:choose>
                                <c:when test="${auth_type eq 'cancellation'}">
                                    <strong>${app}</strong> requests consent to cancel a bulk payment transaction
                                    ${intentSubText}
                                </c:when>
                                <c:otherwise>
                                    <strong>${app}</strong> requests consent to do a bulk payment transaction
                                    ${intentSubText}
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:when test="${consent_type eq 'periodic-payments'}">
                            <c:choose>
                                <c:when test="${auth_type eq 'cancellation'}">
                                    <strong>${app}</strong> requests consent to cancel a periodic payment transaction ${intentSubText}
                                </c:when>
                                <c:otherwise>
                                    <strong>${app}</strong> requests consent to do a periodic payment transaction ${intentSubText}
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                    </c:choose>
                    </h3>
    
                    <h4 class="section-heading-5 ui subheading">Data requested:</h4>
                    <%-- Display requested data --%>
                    <c:forEach items="<%=consentData%>" var="record">
                        <div class="padding" style="border:1px solid #555;">
                            <b>${record.key}</b>
                            <ul class="scopes-list padding">
                                <c:forEach items="${record.value}" var="record_data">
                                    <li>${record_data}</li>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:forEach>

                    <%-- Display accounts data --%>
                    <c:if test="${isStaticBulk}">
                        <div class="padding" style="border:1px solid #555;">
                            <b>Requested Permissions:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=staticBulkMap.get("permissions")%>' var="permission">
                                    <li>${permission}</li>
                                </c:forEach>
                            </ul>
                            <b>On following accounts:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=staticBulkMap.get("accountRefs")%>' var="accountRef">
                                    <li>${accountRef}</li>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:if>

                    <c:if test="${isSelectAccount}">
                        <div class="padding" style="border:1px solid #555;">
                            <b>Requested Permissions:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=selectAccountMap.get("permissions")%>' var="permission">
                                    <li>${permission}</li>
                                </c:forEach>
                            </ul>
                            <b>Select the accounts you wish to authorise:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=selectAccountMap.get("accountRefs")%>' var="accountRef">
                                    <label for="${accountRef}:selectAccount">
                                        <input type="checkbox" id="${accountRef}:selectAccount"
                                            name="checkedAccountsAccountRefs" value="${accountRef}"
                                        />
                                        ${accountRef}
                                    </label><br/>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:if>

                    <c:if test="${isStaticAccount}">
                        <div class="padding" style="border:1px solid #555;">
                            <b>Requested Permissions:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=staticAccountMap.get("permissions")%>' var="permission">
                                    <li>${permission}</li>
                                </c:forEach>
                            </ul>
                            <b>On following accounts:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=staticAccountMap.get("accountRefs")%>' var="accountRef">
                                    <li>${accountRef}</li>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:if>

                    <c:if test="${isSelectBalance}">
                        <div class="padding" style="border:1px solid #555;">
                            <b>Requested Permissions:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=selectBalanceMap.get("permissions")%>' var="permission">
                                    <li>${permission}</li>
                                </c:forEach>
                            </ul>
                            <b>Select the accounts you wish to authorise:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=selectBalanceMap.get("accountRefs")%>' var="accountRef">
                                    <label for="${accountRef}:selectBalance">
                                        <input type="checkbox" id="${accountRef}:selectBalance"
                                            name="checkedBalancesAccountRefs" value="${accountRef}"
                                        />
                                        ${accountRef}
                                    </label><br/>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:if>

                    <c:if test="${isStaticBalance}">
                        <div class="padding" style="border:1px solid #555;">
                            <b>Requested Permissions:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=staticBalanceMap.get("permissions")%>' var="permission">
                                    <li>${permission}</li>
                                </c:forEach>
                            </ul>
                            <b>On following accounts:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=staticBalanceMap.get("accountRefs")%>' var="accountRef">
                                    <li>${accountRef}</li>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:if>

                    <c:if test="${isSelectTransaction}">
                        <div class="padding" style="border:1px solid #555;">
                            <b>Requested Permissions:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=selectTransactionMap.get("permissions")%>' var="permission">
                                    <li>${permission}</li>
                                </c:forEach>
                            </ul>
                            <b>Select the accounts you wish to authorise:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=selectTransactionMap.get("accountRefs")%>' var="accountRef">
                                    <label for="${accountRef}:selectTransaction">
                                        <input type="checkbox" id="${accountRef}:selectTransaction"
                                            name="checkedTransactionsAccountRefs" value="${accountRef}"
                                        />
                                        ${accountRef}
                                    </label><br/>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:if>

                    <c:if test="${isStaticTransaction}">
                        <div class="padding" style="border:1px solid #555;">
                            <b>Requested Permissions:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=staticTransactionMap.get("permissions")%>' var="permission">
                                    <li>${permission}</li>
                                </c:forEach>
                            </ul>
                            <b>On following accounts:</b>
                            <ul class="scopes-list padding">
                                <c:forEach items='<%=staticTransactionMap.get("accountRefs")%>' var="accountRef">
                                    <li>${accountRef}</li>
                                </c:forEach>
                            </ul>
                        </div>
                    </c:if>
                </div>
            </div>
    
            <div class="form-group ui form">
                <div class="col-md-12 ui box">
                    If you want to stop sharing data, you can request us to stop sharing data on your data sharing
                    dashboard.
                    </br>
                    Do you confirm that we can share your data with ${app}?
                </div>
            </div>
            
            <div class="form-group ui form row">
                <div class="ui body col-md-12">
                    <input type="button" class="btn btn-primary" id="approve" name="approve"
                           onclick="javascript: approvedBerlinConsent(); return false;"
                           value="Confirm"/>
                    <input class="btn btn-primary" type="reset" value="Deny"
                           onclick="javascript: deny(); return false;"/>
                    <input type="hidden" name="sessionDataKeyConsent" value="${sessionDataKeyConsent}"/>
                    <input type="hidden" name="consent" id="consent" value="deny"/>
                    <input type="hidden" name="app" id="app" value="${app}"/>
                    <input type="hidden" name="type" id="type" value="${consent_type}"/>
                </div>
            </div>
            
            <div class="form-group ui form row">
                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                    <div class="well policy-info-message" role="alert margin-top-5x">
                        <div>
                            ${privacyDescription}
                            <a href="privacy_policy.do" target="policy-pane">
                                ${privacyGeneral}
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </form>
</div>
<jsp:include page="includes/consent_bottom.jsp"/>
