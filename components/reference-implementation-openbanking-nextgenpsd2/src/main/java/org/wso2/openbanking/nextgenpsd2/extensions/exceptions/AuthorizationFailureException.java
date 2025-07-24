package org.wso2.openbanking.nextgenpsd2.extensions.exceptions;

import org.json.JSONObject;
import org.wso2.openbanking.nextgenpsd2.extensions.utils.ErrorUtil;

/**
 * Exception class for success responses carrying FailedResponseInConsentAuthorize objects.
 */
public class AuthorizationFailureException extends Exception {
    String message;
    String newStatus;
    String responseId;

    public AuthorizationFailureException(String message) {
        super(message);
        this.message = message;
    }

    public AuthorizationFailureException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    public AuthorizationFailureException(String message, String newStatus) {
        super(message);
        this.message = message;
        this.newStatus = newStatus;
    }

    public AuthorizationFailureException(String message, String newStatus, Throwable e) {
        super(message, e);
        this.message = message;
        this.newStatus = newStatus;
    }

    /**
     * Sets response id for the failed response in consent authorize.
     *
     * @param responseId
     */
    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    /**
     * Returns exception formatted as a FailedResponseInConsentAuthorize.
     * @return
     */
    public JSONObject getFormattedError() {
        return ErrorUtil.getFormattedAuthorizationFailureException(this.responseId, this.message, this.newStatus);
    }

    /**
     * Returns formatted error as String.
     * @return
     */
    public String getFormattedErrorAsString() {
        return getFormattedError().toString();
    }
}
