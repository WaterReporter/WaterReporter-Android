package com.viableindustries.waterreporter.data.objects.favorite;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.data.objects.CollectionProperties;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 8/17/17.
 */

public class FavoriteCollection {

    @SerializedName("features")
    private ArrayList<Favorite> features;

    @SerializedName("properties")
    private CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public ArrayList<Favorite> getFeatures() {

        return features;

    }

    public CollectionProperties getProperties() {

        return properties;

    }

}
