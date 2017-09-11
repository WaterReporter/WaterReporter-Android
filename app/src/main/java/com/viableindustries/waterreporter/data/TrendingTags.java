package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 4/21/17.
 */

public class TrendingTags {

    @SerializedName("num_results")
    private int num_results;

    @SerializedName("objects")
    private final ArrayList<HashTag> objects;

    @SerializedName("page")
    private int page;

    @SerializedName("total_pages")
    private int total_pages;

    public ArrayList<HashTag> getFeatures() {

        return objects;

    }

}
