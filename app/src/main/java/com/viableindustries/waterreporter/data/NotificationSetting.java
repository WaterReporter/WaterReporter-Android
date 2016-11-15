package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 11/15/16.
 */

public class NotificationSetting {

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("value")
    public boolean value;

    public NotificationSetting(String aName, String aDescription, boolean aValue) {

        this.name = aName;
        this.description = aDescription;
        this.value = aValue;

    }

}
