package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 11/15/16.
 */

public class NotificationSetting {

    @SerializedName("name")
    public final String name;

    @SerializedName("description")
    public final String description;

    @SerializedName("value")
    public final boolean value;

    public NotificationSetting(String aName, String aDescription, boolean aValue) {

        this.name = aName;
        this.description = aDescription;
        this.value = aValue;

    }

}
