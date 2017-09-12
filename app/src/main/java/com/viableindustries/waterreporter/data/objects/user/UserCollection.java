package com.viableindustries.waterreporter.data.objects.user;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.data.objects.CollectionProperties;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 11/13/16.
 */

public class UserCollection {

    @SerializedName("features")
    private ArrayList<User> features;

    @SerializedName("properties")
    private CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public ArrayList<User> getFeatures() {

        return features;

    }

    public CollectionProperties getProperties() {

        return properties;

    }

}
