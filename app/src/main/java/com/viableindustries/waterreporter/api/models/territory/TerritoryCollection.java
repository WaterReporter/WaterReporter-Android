package com.viableindustries.waterreporter.api.models.territory;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.CollectionProperties;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 2/21/17.
 */

public class TerritoryCollection {

    @SerializedName("objects")
    private ArrayList<Territory> features;

    @SerializedName("properties")
    private CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public ArrayList<Territory> getFeatures() {

        return features;

    }

}
