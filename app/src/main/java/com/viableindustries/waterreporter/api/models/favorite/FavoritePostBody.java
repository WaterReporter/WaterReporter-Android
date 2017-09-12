package com.viableindustries.waterreporter.api.models.favorite;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 7/27/17.
 */

public class FavoritePostBody {

    @SerializedName("report_id")
    private int reportId;

    public FavoritePostBody(int reportId) {
        this.reportId = reportId;
    }

}
