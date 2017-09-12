package com.viableindustries.waterreporter.data.objects.auth;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public class LogInBody {

    @SerializedName("email")
    String email;

    @SerializedName("password")
    String password;

    @SerializedName("response_type")
    String response_type;

    @SerializedName("client_id")
    String client_id;

    @SerializedName("redirect_uri")
    String redirect_uri;

    @SerializedName("scope")
    String scope;

    @SerializedName("state")
    public String state;

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