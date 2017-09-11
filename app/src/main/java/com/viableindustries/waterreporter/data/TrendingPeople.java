package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 4/21/17.
 */

public class TrendingPeople {

    @SerializedName("num_results")
    private int num_results;

    @SerializedName("objects")
    private final ArrayList<User> objects;

    @SerializedName("page")
    private int page;

    @SerializedName("total_pages")
    private int total_pages;

    public ArrayList<User> getFeatures() {

        return objects;

    }

}
