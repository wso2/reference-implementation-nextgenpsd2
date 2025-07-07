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

package org.wso2.openbanking.berlin.consent.extensions.manage.impl;

import com.wso2.openbanking.accelerator.consent.extensions.common.ConsentException;
import com.wso2.openbanking.accelerator.consent.extensions.common.ResponseStatus;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageData;
import com.wso2.openbanking.accelerator.consent.extensions.manage.model.ConsentManageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.openbanking.berlin.common.constants.ErrorConstants;
import org.wso2.openbanking.berlin.common.models.TPPMessage;
import org.wso2.openbanking.berlin.common.utils.ErrorUtil;
import org.wso2.openbanking.berlin.consent.extensions.common.ConsentExtensionConstants;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.ServiceHandler;
import org.wso2.openbanking.berlin.consent.extensions.manage.handler.service.factory.ServiceHandlerFactory;
import org.wso2.openbanking.berlin.consent.extensions.manage.util.CommonConsentUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Consent Manage handler implementation for Berlin.
 */
public class BerlinConsentManageHandler implements ConsentManageHandler {

    private static final Log log = LogFactory.getLog(BerlinConsentManageHandler.class);
    private ServiceHandler serviceHandler;

    @Override
    public void handleGet(ConsentManageData consentManageData) throws ConsentException {

        log.debug("Validating the X-Request-ID header");
        CommonConsentUtil.validateXRequestId(consentManageData.getHeaders());
        consentManageData.setResponseHeader(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                consentManageData.getHeaders().get(ConsentExtensionConstants.X_REQUEST_ID_HEADER));

        serviceHandler = ServiceHandlerFactory.getServiceHandler(consentManageData.getRequestPath());

        if (serviceHandler != null) {
            serviceHandler.handleGet(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
            throw new ConsentException(ResponseStatus.NOT_FOUND, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, null, ErrorConstants.PATH_INVALID));
        }
    }

    @Override
    public void handlePost(ConsentManageData consentManageData) throws ConsentException {

        // if consent initiation, add sp properties to app
        setSpPropertiesToApplication(consentManageData);

        log.debug("Validating the X-Request-ID header");
        CommonConsentUtil.validateXRequestId(consentManageData.getHeaders());
        consentManageData.setResponseHeader(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                consentManageData.getHeaders().get(ConsentExtensionConstants.X_REQUEST_ID_HEADER));

        serviceHandler = ServiceHandlerFactory.getServiceHandler(consentManageData.getRequestPath());

        if (serviceHandler != null) {
            serviceHandler.handlePost(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
            throw new ConsentException(ResponseStatus.NOT_FOUND, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, null, ErrorConstants.PATH_INVALID));
        }
    }

    @Override
    public void handleDelete(ConsentManageData consentManageData) throws ConsentException {

        log.debug("Validating the X-Request-ID header");
        CommonConsentUtil.validateXRequestId(consentManageData.getHeaders());
        consentManageData.setResponseHeader(ConsentExtensionConstants.X_REQUEST_ID_PROPER_CASE_HEADER,
                consentManageData.getHeaders().get(ConsentExtensionConstants.X_REQUEST_ID_HEADER));

        serviceHandler = ServiceHandlerFactory.getServiceHandler(consentManageData.getRequestPath());

        if (serviceHandler != null) {
            serviceHandler.handleDelete(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
            throw new ConsentException(ResponseStatus.NOT_FOUND, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, null, ErrorConstants.PATH_INVALID));
        }
    }

    @Override
    public void handlePut(ConsentManageData consentManageData) throws ConsentException {

        serviceHandler = ServiceHandlerFactory.getServiceHandler(consentManageData.getRequestPath());

        if (serviceHandler != null) {
            serviceHandler.handlePut(consentManageData);
        } else {
            log.error(ErrorConstants.PATH_INVALID);
            throw new ConsentException(ResponseStatus.NOT_FOUND, ErrorUtil.constructBerlinError(
                    null, TPPMessage.CategoryEnum.ERROR, null, ErrorConstants.PATH_INVALID));
        }
    }

    @Override
    public void handlePatch(ConsentManageData consentManageData) throws ConsentException {
        log.error(ErrorConstants.PATCH_NOT_SUPPORTED);
        throw new ConsentException(ResponseStatus.METHOD_NOT_ALLOWED, ErrorConstants.PATCH_NOT_SUPPORTED);
    }

    protected ApplicationManagementServiceImpl getApplicationMgmtServiceImpl() {

        return ApplicationManagementServiceImpl.getInstance();
    }

    private void setSpPropertiesToApplication(ConsentManageData consentManageData) {
        Map<String, String> headers = consentManageData.getHeaders();
        if (consentManageData.getClientId().isEmpty() &&
                headers.containsKey(ConsentExtensionConstants.CLIENT_ID_HEADER)) {
            consentManageData.setClientId(headers.get(ConsentExtensionConstants.CLIENT_ID_HEADER));
        }
        if (headers.containsKey(ConsentExtensionConstants.UPDATE_SP_HEADER)
                && Boolean.parseBoolean(headers.get(ConsentExtensionConstants.UPDATE_SP_HEADER))) {
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String username = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
            ServiceProvider serviceProvider;
            try {
                serviceProvider = getApplicationMgmtServiceImpl()
                        .getServiceProvider(consentManageData.getClientId(), tenantDomain);
            } catch (IdentityApplicationManagementException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ErrorUtil.constructBerlinError(
                        null, TPPMessage.CategoryEnum.ERROR, null, "INTERNAL_SERVER_ERROR"));
            }
            String x509Certificates = consentManageData.getHeaders().get(ConsentExtensionConstants.TPP_CERT_HEADER);
            serviceProvider.setCertificateContent(x509Certificates);
            List<ServiceProviderProperty> serviceProviderPropertyList =
                    new ArrayList<>(Arrays.asList(serviceProvider.getSpProperties()));
            if (!headers.getOrDefault(ConsentExtensionConstants.REGULATORY_HEADER, null).isEmpty()) {
                ServiceProviderProperty regulatory = new ServiceProviderProperty();
                regulatory.setName(ConsentExtensionConstants.REGULATORY);
                regulatory.setValue(headers.get(ConsentExtensionConstants.REGULATORY_HEADER));
                regulatory.setDisplayName(ConsentExtensionConstants.REGULATORY);
                serviceProviderPropertyList.add(regulatory);
            }
            if (!headers.getOrDefault(ConsentExtensionConstants.TPP_ROLES_HEADER, null).isEmpty()) {
                ServiceProviderProperty tppRoles = new ServiceProviderProperty();
                tppRoles.setName("sp_certificate_roles");
                tppRoles.setValue(headers.get(ConsentExtensionConstants.TPP_ROLES_HEADER));
                tppRoles.setDisplayName("SP certificate roles");
                serviceProviderPropertyList.add(tppRoles);
            }
            ServiceProviderProperty[] serviceProviderProperties =
                    new ServiceProviderProperty[serviceProviderPropertyList.size()];
            serviceProviderPropertyList.toArray(serviceProviderProperties);
            serviceProvider.setSpProperties(serviceProviderProperties);
            try {
                getApplicationMgmtServiceImpl().updateApplication(serviceProvider, tenantDomain, username);
            } catch (IdentityApplicationManagementException e) {
                throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, ErrorUtil.constructBerlinError(
                        null, TPPMessage.CategoryEnum.ERROR, null, "INTERNAL_SERVER_ERROR"));
            }
        }
    }

}
