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

    public void setDescription(User user){

        String s = "";

        if (user.properties.description != null && user.properties.description.length() > 0){

            s += user.properties.description.trim();

        }

        this.description = s;

    }

    public String getTitle(){
        return title;
    }

    public void setTitle(User user){

        String s = "";

        if (user.properties.title != null && user.properties.title.length() > 0){

            s += user.properties.title.trim();

        }

        if (s.length() > 0 && user.properties.organization_name != null && user.properties.organization_name.length() > 0){

            s += String.format(" at %s", user.properties.organization_name.trim());

        }

        this.title = s;

    }

    public String getName(){
        return name;
    }
    
    public void setName(User user){

        String s = "";

        if (user.properties.first_name != null && user.properties.first_name.length() > 0) {

            s += user.properties.first_name.trim();

        }

        if (user.properties.last_name != null && user.properties.last_name.length() > 0) {

            s += String.format(" %s", user.properties.last_name.trim());

        }

        if (s.length() < 1) {

            s += "Anonymous";

        }

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

            s += user.properties.organization_name;

        }

        this.organization = s;

    }

    public void setData(User user){

        // Set avatar URL

        this.setAvatarUrl(user.properties.images.get(0).properties.thumbnail_retina);

        // Set user name

        this.setName(user);

        // Set user title

        this.setTitle(user);

        // Set user description

        this.setDescription(user);

    }

}
