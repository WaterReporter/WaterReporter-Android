package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by brendanmcintyre on 7/20/17.
 */

public class HUCProperties implements Serializable {

    @SerializedName("code")
    public String code;

    @SerializedName("name")
    public String name;

    @SerializedName("bounds")
    public List<Double> bounds;

    @SerializedName("states")
    public HUCStateCollection states;

}
