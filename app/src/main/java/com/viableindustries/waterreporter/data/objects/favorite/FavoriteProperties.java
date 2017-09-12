package com.viableindustries.waterreporter.data.objects.favorite;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.data.objects.user.User;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 7/27/17.
 */

public class FavoriteProperties implements Serializable {

    @SerializedName("id")
    public int id;

    @SerializedName("owner")
    public User owner;

    @SerializedName("owner_id")
    public int owner_id;

    public FavoriteProperties(String aCreated, int aId, int aOwnerId, int aReportId) {
        String created = aCreated;
        this.id = aId;
        this.owner_id = aOwnerId;
        int report_id = aReportId;
    }

}