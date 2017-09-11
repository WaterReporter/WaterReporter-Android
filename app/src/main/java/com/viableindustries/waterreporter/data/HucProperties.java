package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by brendanmcintyre on 7/20/17.
 */

public class HucProperties implements Serializable {

    @SerializedName("code")
    public String code;

    @SerializedName("name")
    public String name;

    @SerializedName("bounds")
    public final List<Double> bounds;

    @SerializedName("states")
    public HucStateCollection states;

}
