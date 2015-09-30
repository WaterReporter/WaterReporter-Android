package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public class LogInBody {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("response_type")
    public String response_type;

    @SerializedName("client_id")
    public String client_id;

    @SerializedName("redirect_uri")
    public String redirect_uri;

    @SerializedName("scope")
    public String scope;

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