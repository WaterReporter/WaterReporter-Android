package com.viableindustries.waterreporter.api.models.group;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.organization.Organization;

/**
 * Created by brendanmcintyre on 10/16/17.
 */

public class GroupProperties {

    @SerializedName("joined_on")
    public String joined;

//    @SerializedName("id")
//    public int id;

    @SerializedName("organization_id")
    public int organizationId;

    @SerializedName("user_id")
    public int userId;

    @SerializedName("is_admin")
    public boolean isAdmin;

    @SerializedName("is_member")
    public boolean isMember;

    @SerializedName("organization")
    public Organization organization;

    public GroupProperties(
            String aJoined,
            int aOrganizationId,
            int aUserId,
            boolean aIsAdmin,
            boolean aIsMember
    ) {

        this.joined = aJoined;
        this.organizationId = aOrganizationId;
        this.userId = aUserId;
        this.isAdmin = aIsAdmin;
        this.isMember = aIsMember;
    }

}