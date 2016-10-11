package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public class UserProperties implements Serializable {

    @SerializedName("active")
    public Boolean active;

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

    @SerializedName("organization")
    public ArrayList<Organization> organizations;

    @SerializedName("organization_name")
    public String organization_name;

    @SerializedName("picture")
    public String picture;

    @SerializedName("public_email")
    public String public_email;

    @SerializedName("title")
    public String title;

    public UserProperties createProperties(Boolean aActive, String aDescription, String aEmail, int aId,
                                           String aLastName, String aFirstName, int aLoginCount,
                                           ArrayList<Organization> aOrganizations, String aOrganizationName,
                                           String aPicture, String aPublicEmail, String aTitle){

        this.active = aActive;
        this.description = aDescription;
        this.email = aEmail;
        this.first_name = aFirstName;
        this.id = aId;
        this.last_name = aLastName;
        this.login_count = aLoginCount;
        this.organizations = aOrganizations;
        this.organization_name = aOrganizationName;
        this.picture = aPicture;
        this.public_email = aPublicEmail;
        this.title = aTitle;

        return this;

    }

}
