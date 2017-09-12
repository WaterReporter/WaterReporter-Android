package com.viableindustries.waterreporter.api.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public class CollectionProperties {

    @SerializedName("num_results")
    public int num_results;

    @SerializedName("total_pages")
    public int total_pages;

    @SerializedName("page")
    public int page;

}
