package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 8/28/15.
 */
public class AuthResponse implements Serializable {

    @SerializedName("access_token")
    private String access_token;

    @SerializedName("expires_in")
    private String expires_in;

    @SerializedName("scope")
    private String scope;

    @SerializedName("state")
    private String state;

    @SerializedName("token_type")
    private String token_type;

    public String getAccessToken() {

        return access_token;

    }

}
