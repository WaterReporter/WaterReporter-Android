package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public class UserProperties implements Serializable {

    //@SerializedName("active")
    //public Boolean active;

    @SerializedName("description")
    public String description;

    //@SerializedName("email")
    //public String email;

    @SerializedName("first_name")
    public String first_name;

    @SerializedName("id")
    public int id;

    @SerializedName("images")
    public ArrayList<ReportPhoto> images;

    @SerializedName("last_name")
    public String last_name;

    //@SerializedName("login_count")
    //public int login_count;

    @SerializedName("organization")
    public ArrayList<Organization> organizations;

    @SerializedName("organization_name")
    public String organization_name;

    @SerializedName("picture")
    public String picture;

    @SerializedName("public_email")
    public String public_email;

    @SerializedName("roles")
    public ArrayList<Role> roles;

    @SerializedName("title")
    public String title;

    public UserProperties (int aId, String aDescription, String aFirstName,
                           String aLastName, String aOrganizationName, String aPicture,
                           String aPublicEmail, String aTitle, ArrayList<ReportPhoto> aImages,
                           ArrayList<Organization> aOrganizations, ArrayList<Role> aRoles){

        //this.active = aActive;
        this.description = aDescription;
        //this.email = aEmail;
        this.first_name = aFirstName;
        this.id = aId;
        this.images = aImages;
        this.last_name = aLastName;
        //this.login_count = aLoginCount;
        this.organizations = aOrganizations;
        this.organization_name = aOrganizationName;
        this.picture = aPicture;
        this.public_email = aPublicEmail;
        this.roles = aRoles;
        this.title = aTitle;

        //return this;

    }

    public Map<String, String> getStringProperties(){

        Map<String, String> stringProperties = new HashMap<String, String>();

        stringProperties.put("description", this.description);
        stringProperties.put("first_name", this.first_name);
        stringProperties.put("last_name", this.last_name);
        stringProperties.put("organization_name", this.organization_name);
        stringProperties.put("picture", this.picture);
        stringProperties.put("public_email", this.public_email);
        stringProperties.put("title", this.title);

        return stringProperties;

    }

}
