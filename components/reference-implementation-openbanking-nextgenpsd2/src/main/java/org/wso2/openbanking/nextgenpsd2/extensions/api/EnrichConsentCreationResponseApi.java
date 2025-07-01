package org.wso2.openbanking.nextgenpsd2.extensions.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.datamodels.TPPMessage;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.ServerException;
import org.wso2.openbanking.nextgenpsd2.extensions.model.EnrichConsentCreationRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.ErrorResponse;
import org.wso2.openbanking.nextgenpsd2.extensions.model.Response200ForResponseAlternation;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;
import org.wso2.openbanking.nextgenpsd2.extensions.model.SuccessResponseForResponseAlternation;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.CommonConsentValidationUtil;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ConsentExtensionConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ConsentResponseHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ErrorConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ErrorUtil;

import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/enrich-consent-creation-response")
@Api(description = "the enrich-consent-creation-response API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-05-05T12:44:23.299724+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class EnrichConsentCreationResponseApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "handle post-consent generation -response generation,validations", notes = "", response = Response200ForResponseAlternation.class, authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Consent" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = Response200ForResponseAlternation.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response enrichConsentCreationResponsePost(@Valid @NotNull EnrichConsentCreationRequestBody requestBody) {
        Log log = LogFactory.getLog(PreProcessConsentCreationApi.class);
        SuccessResponseForResponseAlternation validationResponse = new SuccessResponseForResponseAlternation();

        try {
            ConsentResponseHandler consentHandler = CommonConsentValidationUtil.getConsentResponseHandler(requestBody.getData()
                    .getConsentResourcePath());

            if (consentHandler != null) {
                consentHandler.enrichCreationResponse(requestBody, validationResponse);
            } else {
                // Server error since if path is invalid consent creation should have failed
                // thus making this unreachable
                throw new ServerException(ServerException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR, null,
                                ErrorConstants.PATH_INVALID));
            }

        } catch (ServerException e) {
            log.error(e);
            return Response.status(e.getStatus()).entity(e.getFormattedErrorAsString()).build();

        } catch (JSONException e) {
            log.error(e);
            return Response.status(Response.Status.BAD_REQUEST).entity(new JSONObject(
                    ErrorUtil.getErrorResponse(ConsentExtensionConstants.INVALID_REQUEST, e.getMessage())
            ).toString()).build();

        } catch (Exception e) {
            log.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new JSONObject(
                    ErrorUtil.getErrorResponse(ConsentExtensionConstants.SERVER_ERROR, e.getMessage())
            ).toString()).build();
        }

        return Response.ok().entity(new JSONObject(validationResponse).toString()).build();
    }
}
