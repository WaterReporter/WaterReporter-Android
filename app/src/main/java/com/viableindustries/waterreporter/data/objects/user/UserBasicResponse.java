package com.viableindustries.waterreporter.data.objects.user;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 9/2/15.
 */

// Response to successful requests for http://stg.stg.api.waterreporter.org/v1/data/me

public class UserBasicResponse {

    @SerializedName("first_name")
    private String first_name;

    @SerializedName("id")
    private int id;

    @SerializedName("last_name")
    private String last_name;

    @SerializedName("picture")
    private String picture;

    @SerializedName("roles")
    private List<Map<String, ?>> roles;

    public Integer getUserId() {

        return id;

    }

}