package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 11/14/16.
 */

public class ReportStateBody {

    @SerializedName("id")
    private final int id;

    @SerializedName("state")
    private final String state;

    public ReportStateBody (int aId, String aState) {

        this.id = aId;

        this.state = aState;

    }

}
