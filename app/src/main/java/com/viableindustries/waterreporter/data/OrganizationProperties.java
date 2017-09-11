package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public class OrganizationProperties implements Serializable {

    @SerializedName("created")
    public String created;

    @SerializedName("description")
    public final String description;

    @SerializedName("email")
    public String email;

    @SerializedName("id")
    public final int id;

    @SerializedName("name")
    public final String name;

    @SerializedName("picture")
    public final String picture;

    @SerializedName("status")
    public String status;

    @SerializedName("users")
    public User[] users;

    @SerializedName("website")
    public String website;

}
