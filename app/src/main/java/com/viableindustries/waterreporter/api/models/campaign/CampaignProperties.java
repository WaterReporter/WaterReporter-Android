package com.viableindustries.waterreporter.api.models.campaign;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.post.ReportPhoto;
import com.viableindustries.waterreporter.api.models.user.User;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/21/18.
 */

public class CampaignProperties {

    @SerializedName("created")
    public String created;

    @SerializedName("description")
    public String description;

    @SerializedName("expiration_date")
    public String expiration_date;

    @SerializedName("form")
    public CampaignForm form;

    @SerializedName("has_expiration")
    public Boolean has_expiration;

    @SerializedName("id")
    public int id;

    @SerializedName("images")
    public List<ReportPhoto> images;

    @SerializedName("is_curated")
    public Boolean is_curated;

    @SerializedName("is_featured")
    public Boolean is_featured;

    @SerializedName("name")
    public String name;

    @SerializedName("organizations")
    public List<Organization> organizations;

    @SerializedName("organizers")
    public List<User> organizers;

    @SerializedName("picture")
    public String picture;

    @SerializedName("tagline")
    public String tagline;

    @SerializedName("users")
    public List<User> users;

    @SerializedName("website")
    public String website;

}
