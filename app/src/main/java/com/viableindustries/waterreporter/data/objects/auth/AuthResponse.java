package com.viableindustries.waterreporter.data.objects.auth;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public class AuthResponse implements Serializable {

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