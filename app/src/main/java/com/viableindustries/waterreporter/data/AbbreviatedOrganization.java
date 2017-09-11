package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 11/15/16.
 */

public class AbbreviatedOrganization {

    @SerializedName("id")
    public final int id;

    @SerializedName("name")
    public final String name;

    public AbbreviatedOrganization (int aId, String aName) {

        this.id = aId;
        this.name= aName;

    }

}
