package com.viableindustries.waterreporter.data.objects.territory;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by brendanmcintyre on 7/20/17.
 */

public class HucStateCollection implements Serializable {

    @SerializedName("concat")
    public String concat;

    @SerializedName("features")
    public List<HucState> features;

}
