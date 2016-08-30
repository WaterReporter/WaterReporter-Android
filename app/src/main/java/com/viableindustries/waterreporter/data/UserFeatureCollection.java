package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 8/30/16.
 */

public class UserFeatureCollection {

    @SerializedName("features")
    private ArrayList<User> features;

    @SerializedName("properties")
    private CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public ArrayList<User> getFeatures() {

        return features;

    }

}
