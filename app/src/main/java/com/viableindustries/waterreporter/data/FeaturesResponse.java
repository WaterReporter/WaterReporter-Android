package com.viableindustries.waterreporter.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Ryan Hamley on 10/6/14.
 * This class defines the response type accepted in CommonsCloudResponse.
 */
public class FeaturesResponse {

    public List<Report> features;

}
