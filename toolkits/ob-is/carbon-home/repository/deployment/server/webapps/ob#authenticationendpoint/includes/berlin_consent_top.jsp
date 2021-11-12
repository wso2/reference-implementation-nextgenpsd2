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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="java.util.stream.Stream" %>

<%@ taglib prefix = "fmt" uri = "http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <jsp:include page="head.jsp"/>
    <script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
    <script src="js/auth-functions.js"></script>
    <script src="js/berlin-auth-functions.js"></script>
</head>

<body>

<div class="page-content-wrapper" style="position: relative; min-height: 100vh;">
    <div class="container-fluid " style="padding-bottom: 40px">
        <div class="container">
            <div class="login-form-wrapper">
                <div class="row">
                    <img src="images/logo-dark.svg"
                         class="img-responsive brand-spacer login-logo" alt="WSO2 Open Banking"/>
                </div>
                
                <div class="row data-container">
                    