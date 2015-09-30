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
    private Map<String, Map<String, String>> response;

    public String getAccessToken() {

        return response.get("user").get("authentication_token");

    }

    public Integer getUserId() {

        return Integer.parseInt(response.get("user").get("id"));

    }

}