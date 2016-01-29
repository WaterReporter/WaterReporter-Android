package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public class OrganizationFeatureCollection {

    @SerializedName("features")
    private ArrayList<Organization> features;

    @SerializedName("properties")
    private CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public ArrayList<Organization> getFeatures() {

        return features;

    }

}
