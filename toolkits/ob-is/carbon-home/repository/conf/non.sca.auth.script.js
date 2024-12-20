var psuChannel = 'Online Banking';

var onLoginRequest = function(context) {
    publishAuthData(context, "AuthenticationAttempted", {'psuChannel': psuChannel});
    executeStep(1, {
        onSuccess: function (context) {
            OBAuthenticationWorker(context, {}, "psuAuthenticated");
            Log.info("Authentication Successful");
            publishAuthData(context, "AuthenticationSuccessful", {'psuChannel': psuChannel});
            OBAuthenticationWorker(context, {}, "finalised");
        },
        onFail: function (context) {
            Log.info("Authentication Failed");
            publishAuthData(context, "AuthenticationFailed", {'psuChannel': psuChannel});
            OBAuthenticationWorker(context, {}, "failed");
        }
    });
    
};
