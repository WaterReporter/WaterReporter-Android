package com.viableindustries.waterreporter.data.objects.organization;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 4/21/17.
 */

public class TrendingGroups {

    @SerializedName("num_results")
    private int num_results;

    @SerializedName("objects")
    private ArrayList<Organization> objects;

    @SerializedName("page")
    private int page;

    @SerializedName("total_pages")
    private int total_pages;

    public ArrayList<Organization> getFeatures() {

        return objects;

    }

}
