package com.viableindustries.waterreporter.api.models.campaign;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.CollectionProperties;
import com.viableindustries.waterreporter.api.models.organization.Organization;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 3/21/18.
 */

public class CampaignCollection {

    @SerializedName("features")
    ArrayList<Campaign> features;

    @SerializedName("properties")
    CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public ArrayList<Campaign> getFeatures() {

        return features;

    }

    public CollectionProperties getProperties() {

        return properties;

    }

}
