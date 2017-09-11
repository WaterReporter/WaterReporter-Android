package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 7/20/17.
 */

class HucState implements Serializable {

    @SerializedName("code")
    public String code;

    @SerializedName("name")
    public String name;

}
