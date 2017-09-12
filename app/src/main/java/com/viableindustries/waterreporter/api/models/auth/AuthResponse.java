package com.viableindustries.waterreporter.api.models.auth;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public class AuthResponse {

    @SerializedName("access_token")
    String access_token;

    @SerializedName("expires_in")
    String expires_in;

    @SerializedName("scope")
    String scope;

    @SerializedName("state")
    String state;

    @SerializedName("token_type")
    String token_type;

    public String getAccessToken() {

        return access_token;

    }

}