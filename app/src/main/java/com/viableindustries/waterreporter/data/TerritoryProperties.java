package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public class TerritoryProperties implements Serializable {

    @SerializedName("huc_10_code")
    public long huc_10_code;

    @SerializedName("huc_10_name")
    public String huc_10_name;

    @SerializedName("huc_12_code")
    public long huc_12_code;

    @SerializedName("huc_12_name")
    public String huc_12_name;

    @SerializedName("huc_6_code")
    public long huc_6_code;

    @SerializedName("huc_6_name")
    public String huc_6_name;

    @SerializedName("huc_8_code")
    public final int huc_8_code;

    @SerializedName("huc_8_name")
    public final String huc_8_name;

    @SerializedName("id")
    public int id;

}
