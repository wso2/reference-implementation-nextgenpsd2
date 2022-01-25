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

package com.wso2.openbanking.berlin.demo.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wso2.openbanking.berlin.demo.backend.beans.fundsconfirmation.FundsConfirmationDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Mock backend to support funds confirmation services.
 */
@Path("/")
public class FundsConfirmationService {

    private static final Log log = LogFactory.getLog(FundsConfirmationService.class);
    private ObjectMapper mapper = new ObjectMapper();

    @POST
    @Consumes({"application/json; charset=utf-8"})
    @Path("/funds-confirmations")
    @Produces("application/json")
    public Response getAccountBalance(String requestString,
                                      @HeaderParam("X-Request-ID") String xRequestId) {
        try {
            mapper.readValue(requestString, FundsConfirmationDTO.class);
        } catch (IOException e) {
            log.error("Error in casting JSON body " + e.toString());
            return Response.status(400).header("X-Request-ID", xRequestId).build();
        }

        String response = "{\"fundsAvailable\": true}";

        return Response.status(200).entity(response)
                .header("X-Request-ID", xRequestId).build();
    }


}
