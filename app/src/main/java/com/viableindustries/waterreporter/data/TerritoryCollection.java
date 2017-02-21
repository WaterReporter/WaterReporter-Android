package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 2/21/17.
 */

public class TerritoryCollection {

    @SerializedName("features")
    private ArrayList<Territory> features;

    @SerializedName("properties")
    private CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public ArrayList<Territory> getFeatures() {

        return features;

    }

}
