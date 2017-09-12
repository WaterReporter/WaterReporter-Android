package com.viableindustries.waterreporter.data.objects.territory;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 7/20/17.
 */

public class HucState implements Serializable {

    @SerializedName("code")
    public String code;

    @SerializedName("name")
    public String name;

}
