package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 11/13/16.
 */

public class UserCollection {

    @SerializedName("features")
    private final ArrayList<User> features;

    @SerializedName("properties")
    private final CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public ArrayList<User> getFeatures() {

        return features;

    }

    public CollectionProperties getProperties() {

        return properties;

    }

}
