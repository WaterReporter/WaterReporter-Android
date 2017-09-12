package com.viableindustries.waterreporter.api.models.territory;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 4/21/17.
 */

public class TrendingTerritories {

    @SerializedName("num_results")
    private int num_results;

    @SerializedName("objects")
    private ArrayList<Territory> objects;

    @SerializedName("page")
    private int page;

    @SerializedName("total_pages")
    private int total_pages;

    public ArrayList<Territory> getFeatures() {

        return objects;

    }

}
