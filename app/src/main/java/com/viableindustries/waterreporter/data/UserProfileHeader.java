package com.viableindustries.waterreporter.data;

/**
 * Created by brendanmcintyre on 8/4/17.
 */

public class UserProfileHeader {

    private String description;

    private String title;

    private String name;

    private String avatarUrl;

    private String organization;

    public UserProfileHeader(){};

    public String getDescription(){
        return description;
    }

    public void setDescription(String s){
        this.description = s;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(User user){

        String s = "";

        if (user.properties.title != null && user.properties.title.length() > 0){

            s = user.properties.title;

        }

        this.title = s;

    }

    public String getName(){
        return name;
    }
    
    public void setName(String s){
        this.name = s;
    }

    public String getAvatarUrl(){
        return avatarUrl;
    }
    
    public void setAvatarUrl(String s){
        this.avatarUrl = s;
    }

    public String getOrganization(){
        return organization;
    }
    
    public void setOrganization(User user){

        String s = "";

        if (user.properties.organization_name != null && user.properties.organization_name.length() > 0){

            s = user.properties.organization_name;

        }

        this.organization = s;

    }

}
