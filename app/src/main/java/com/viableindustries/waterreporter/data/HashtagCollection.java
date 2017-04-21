package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 4/21/17.
 */

public class HashtagCollection {

    @SerializedName("features")
    private ArrayList<HashTag> features;

    @SerializedName("properties")
    private CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public ArrayList<HashTag> getFeatures() {

        return features;

    }

    public CollectionProperties getProperties() {

        return properties;

    }

}
