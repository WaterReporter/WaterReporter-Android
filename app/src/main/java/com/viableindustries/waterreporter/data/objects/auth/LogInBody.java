package com.viableindustries.waterreporter.data.objects.auth;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public class LogInBody {

    public LogInBody (String aEmail, String aPassword, String aResponseType, String aClientId,
                           String aRedirectUri, String aScope, String aState) {

        String email = aEmail;

        String password = aPassword;

        String response_type = aResponseType;

        String client_id = aClientId;

        String redirect_uri = aRedirectUri;

        String scope = aScope;

        String state = aState;

    }

}
