package com.viableindustries.waterreporter.api.models;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.post.Report;

import java.util.List;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class FeatureCollection {

    @SerializedName("features")
    private List<Report> features;

    @SerializedName("properties")
    private CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public List<Report> getFeatures() {

        return features;

    }

    public CollectionProperties getProperties() {

        return properties;

    }

}
