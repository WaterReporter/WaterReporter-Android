package com.viableindustries.waterreporter.api.models.organization;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.user.User;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public class OrganizationProperties {

    @SerializedName("created")
    public String created;

    @SerializedName("description")
    public String description;

    @SerializedName("email")
    public String email;

    @SerializedName("id")
    public int id;

    @SerializedName("name")
    public String name;

    @SerializedName("picture")
    public String picture;

    @SerializedName("status")
    public String status;

    @SerializedName("users")
    public User[] users;

    @SerializedName("website")
    public String website;

}