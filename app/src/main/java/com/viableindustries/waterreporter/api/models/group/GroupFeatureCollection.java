package com.viableindustries.waterreporter.api.models.group;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.CollectionProperties;
import com.viableindustries.waterreporter.api.models.organization.Organization;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 10/16/17.
 */

public class GroupFeatureCollection {

    @SerializedName("features")
    ArrayList<Group> features;

    @SerializedName("properties")
    CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public ArrayList<Group> getFeatures() {

        return features;

    }

    public CollectionProperties getProperties() {

        return properties;

    }

}
