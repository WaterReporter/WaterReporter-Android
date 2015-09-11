package com.viableindustries.waterreporter.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class FeatureCollection implements Serializable {

    @SerializedName("features")
    private List<Report> features;

    @SerializedName("properties")
    private CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public List<Report> getFeatures () {

        return features;

    }

}
