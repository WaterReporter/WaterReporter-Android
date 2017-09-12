package com.viableindustries.waterreporter.data.objects.organization;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.data.objects.CollectionProperties;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public class OrganizationFeatureCollection {

    @SerializedName("features")
    ArrayList<Organization> features;

    @SerializedName("properties")
    CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public ArrayList<Organization> getFeatures() {

        return features;

    }

    public CollectionProperties getProperties() {

        return properties;

    }

}
