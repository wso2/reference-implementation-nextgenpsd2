package com.wso2.openbanking.berlin.extensions.utils;

import com.wso2.openbanking.berlin.extensions.exceptions.FailedValidationException;
import com.wso2.openbanking.berlin.extensions.model.PreProcessConsentCreationRequestBody;
import com.wso2.openbanking.berlin.extensions.model.SuccessResponsePreProcessConsentCreation;

public interface ConsentHandler {

    void handleCreation(PreProcessConsentCreationRequestBody requestBody,
                        SuccessResponsePreProcessConsentCreation validationResponse) throws FailedValidationException;
}
