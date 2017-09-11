package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 7/27/17.
 */

public class FavoriteProperties implements Serializable {

    @SerializedName("created")
    private final String created;

    @SerializedName("id")
    public final int id;

    @SerializedName("owner")
    public User owner;

    @SerializedName("owner_id")
    public final int owner_id;

    @SerializedName("report_id")
    private final int report_id;

    public FavoriteProperties(String aCreated, int aId, int aOwnerId, int aReportId) {
        this.created = aCreated;
        this.id = aId;
        this.owner_id = aOwnerId;
        this.report_id = aReportId;
    }

}