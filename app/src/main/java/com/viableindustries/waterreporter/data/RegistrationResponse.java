package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by brendanmcintyre on 9/2/15.
 */
public class RegistrationResponse {

    @SerializedName("meta")
    private Map<String, Integer> meta;

    @SerializedName("response")
    private Map<String, Map<String, Object>> response;

    public String getAccessToken() {

        return (String) response.get("user").get("authentication_token");

    }

    public Integer getUserId() {

        return Integer.parseInt(response.get("user").get("id").toString());

    }

    public Integer getCode() {

        return meta.get("code");

    }

}
