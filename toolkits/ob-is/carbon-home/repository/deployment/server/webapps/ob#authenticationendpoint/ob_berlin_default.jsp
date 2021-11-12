<%--
  ~ Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
  ~
  ~ This software is the property of WSO2 Inc. and its suppliers, if any.
  ~ Dissemination of any information or reproduction of any material contained
  ~ herein is strictly forbidden, unless permitted by WSO2 in accordance with
  ~ the WSO2 Software License available at https://wso2.com/licenses/eula/3.1.
  ~ For specific language governing the permissions and limitations under this license,
  ~ please see the license as well as any agreement you’ve entered into with
  ~ WSO2 governing the purchase of this software and any associated services.
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
                            <strong>${app}</strong> requests consent to do a bulk payment transaction ${intentSubText}
                        </c:when>
                        <c:when test="${consent_type eq 'periodic-payments'}">
                            <strong>${app}</strong> requests consent to do a periodic payment transaction
                            ${intentSubText}
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
                    <c:choose>
                        <c:when test="${consent_type eq 'accounts'}">
                           <input type="button" class="btn btn-primary" id="back" name="back"
                                  onclick="history.back();"
                                  value="Go Back"/>
                        </c:when>
                    </c:choose>
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