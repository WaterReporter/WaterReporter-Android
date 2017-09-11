package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public class LogInBody {

    @SerializedName("email")
    private final String email;

    @SerializedName("password")
    private final String password;

    @SerializedName("response_type")
    private final String response_type;

    @SerializedName("client_id")
    private final String client_id;

    @SerializedName("redirect_uri")
    private final String redirect_uri;

    @SerializedName("scope")
    private final String scope;

    @SerializedName("state")
    private final String state;

    public LogInBody (String aEmail, String aPassword, String aResponseType, String aClientId,
                           String aRedirectUri, String aScope, String aState) {

        this.email = aEmail;

        this.password = aPassword;

        this.response_type = aResponseType;

        this.client_id = aClientId;

        this.redirect_uri = aRedirectUri;

        this.scope = aScope;

        this.state = aState;

    }

}
