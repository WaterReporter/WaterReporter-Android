package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public class CollectionProperties implements Serializable {

    @SerializedName("num_results")
    public int num_results;

    @SerializedName("total_pages")
    public int total_pages;

    @SerializedName("page")
    public int page;

}
