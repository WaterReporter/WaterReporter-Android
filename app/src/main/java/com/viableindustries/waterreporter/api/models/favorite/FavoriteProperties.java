package com.viableindustries.waterreporter.api.models.favorite;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.user.User;

/**
 * Created by brendanmcintyre on 7/27/17.
 */

public class FavoriteProperties {

    @SerializedName("created")
    public String created;

    @SerializedName("id")
    public int id;

    @SerializedName("owner")
    public User owner;

    @SerializedName("owner_id")
    public int owner_id;

    @SerializedName("report_id")
    public int report_id;

    public FavoriteProperties(String aCreated, int aId, int aOwnerId, int aReportId) {
        this.created = aCreated;
        this.id = aId;
        this.owner_id = aOwnerId;
        this.report_id = aReportId;
    }

}