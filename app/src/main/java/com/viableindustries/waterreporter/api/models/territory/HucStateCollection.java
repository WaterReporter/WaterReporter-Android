package com.viableindustries.waterreporter.api.models.territory;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 7/20/17.
 */

public class HucStateCollection {

    @SerializedName("concat")
    public String concat;

    @SerializedName("features")
    public List<HucState> features;

}
