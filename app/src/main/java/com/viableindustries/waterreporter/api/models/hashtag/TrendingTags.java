package com.viableindustries.waterreporter.api.models.hashtag;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 4/21/17.
 */

public class TrendingTags {

    @SerializedName("num_results")
    private int num_results;

    @SerializedName("objects")
    private ArrayList<HashTag> objects;

    @SerializedName("page")
    private int page;

    @SerializedName("total_pages")
    private int total_pages;

    public ArrayList<HashTag> getFeatures() {

        return objects;

    }

}
