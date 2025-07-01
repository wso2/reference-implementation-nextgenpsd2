package org.wso2.openbanking.nextgenpsd2.extensions.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.datamodels.TPPMessage;
import org.wso2.openbanking.nextgenpsd2.extensions.exceptions.FailedValidationException;
import org.wso2.openbanking.nextgenpsd2.extensions.model.ErrorResponse;
import org.wso2.openbanking.nextgenpsd2.extensions.model.PreProcessConsentCreationRequestBody;
import org.wso2.openbanking.nextgenpsd2.extensions.model.Response200ForPreProcessConsentCreation;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;
import org.wso2.openbanking.nextgenpsd2.extensions.model.SuccessResponsePreProcessConsentCreation;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.CommonConsentValidationUtil;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ConsentExtensionConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ConsentHandler;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ErrorConstants;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ErrorUtil;

import javax.validation.constraints.*;
import javax.validation.Valid;

/**
* Represents a collection of functions to interact with the API endpoints.
*/
@Path("/pre-process-consent-creation")
@Api(description = "the pre-process-consent-creation API")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaJAXRSSpecServerCodegen", date = "2025-05-05T12:44:23.299724+05:30[Asia/Colombo]", comments = "Generator version: 7.12.0")
public class PreProcessConsentCreationApi {

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "handle pre validations & obtain custom consent data to be stored", notes = "", response = Response200ForPreProcessConsentCreation.class, authorizations = {
        @Authorization(value = "OAuth2", scopes = {
             }),
        
        @Authorization(value = "BasicAuth")
         }, tags={ "Consent" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = Response200ForPreProcessConsentCreation.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Server Error", response = ErrorResponse.class)
    })
    public Response preProcessConsentCreationPost(@Valid @NotNull PreProcessConsentCreationRequestBody requestBody) {
        Log log = LogFactory.getLog(PreProcessConsentCreationApi.class);
        SuccessResponsePreProcessConsentCreation validationResponse = new SuccessResponsePreProcessConsentCreation();

        try {
            // Validate X-request-ID header
            // Enable forwarding of the specific header in accelerator configurations
            CommonConsentValidationUtil.validateIdempotencyHeader(requestBody.getData().getRequestHeaders());

            // Extract consent initiation data
            JSONObject consentInitiationDataJSON;
            try {
                consentInitiationDataJSON = CommonConsentValidationUtil.convertObjectToJson(requestBody.getData()
                        .getConsentInitiationData());

                if (consentInitiationDataJSON.isEmpty()) {
                    // If payload is empty
                    throw new FailedValidationException(FailedValidationException.ErrorCode.BAD_REQUEST,
                            ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                                    TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.PAYLOAD_NOT_PRESENT_ERROR));
                }

            } catch (JSONException e) {
                // If payload is not JSON
                throw new FailedValidationException(FailedValidationException.ErrorCode.BAD_REQUEST,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR,
                                TPPMessage.CodeEnum.FORMAT_ERROR, ErrorConstants.PAYLOAD_FORMAT_ERROR));
            }

            ConsentHandler consentHandler = CommonConsentValidationUtil.getConsentHandler(requestBody.getData()
                    .getConsentResourcePath());

            if (consentHandler != null) {
                consentHandler.handleCreation(requestBody, validationResponse);
            } else {
                throw new FailedValidationException(FailedValidationException.ErrorCode.NOT_FOUND,
                        ErrorUtil.constructBerlinError(null, TPPMessage.CategoryEnum.ERROR, null,
                                ErrorConstants.PATH_INVALID));
            }

        } catch (FailedValidationException e) {
            log.error("Validation failed for consent creation. Returning failed response.", e);
            return Response.ok().entity(e.getFormattedErrorAsString()).build();

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
