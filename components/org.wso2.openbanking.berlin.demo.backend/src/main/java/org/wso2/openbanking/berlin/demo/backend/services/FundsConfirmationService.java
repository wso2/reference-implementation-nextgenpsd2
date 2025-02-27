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

package org.wso2.openbanking.berlin.demo.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.openbanking.berlin.demo.backend.beans.fundsconfirmation.FundsConfirmationDTO;

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
