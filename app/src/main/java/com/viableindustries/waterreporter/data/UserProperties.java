package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public class UserProperties implements Serializable {

    @SerializedName("description")
    public String description;

    @SerializedName("email")
    public String email;

    @SerializedName("first_name")
    public String first_name;

    @SerializedName("id")
    public int id;

    @SerializedName("last_name")
    public String last_name;

    @SerializedName("login_count")
    public int login_count;

    @SerializedName("organization_name")
    public String organization_name;

    @SerializedName("picture")
    public String picture;

    @SerializedName("title")
    public String title;

}