package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 7/26/17.
 */

public class OpenGraphObject implements Serializable {

    @SerializedName("properties")
    public final OpenGraphProperties properties;

}
