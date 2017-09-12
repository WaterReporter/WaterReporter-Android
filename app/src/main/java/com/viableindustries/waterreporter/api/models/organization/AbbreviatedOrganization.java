package com.viableindustries.waterreporter.api.models.organization;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 11/15/16.
 */

public class AbbreviatedOrganization {

    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    public AbbreviatedOrganization(int aId, String aName) {

        this.id = aId;
        this.name = aName;

    }

}
