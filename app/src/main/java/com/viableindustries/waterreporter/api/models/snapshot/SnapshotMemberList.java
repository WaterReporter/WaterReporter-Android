package com.viableindustries.waterreporter.api.models.snapshot;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/26/18.
 */

public class SnapshotMemberList {

    @SerializedName("results")
    public List<SnapshotShallowUser> members;

}