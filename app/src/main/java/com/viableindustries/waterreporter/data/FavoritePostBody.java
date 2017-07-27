package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 7/27/17.
 */

public class FavoritePostBody implements Serializable {

    @SerializedName("report_id")
    private int reportId;

    public FavoritePostBody(int reportId) {
        this.reportId = reportId;
    }

}
