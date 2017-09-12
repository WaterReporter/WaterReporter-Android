package com.viableindustries.waterreporter.api.models.hashtag;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.CollectionProperties;

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
