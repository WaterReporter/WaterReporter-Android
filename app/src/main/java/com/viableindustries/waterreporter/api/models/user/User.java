package com.viableindustries.waterreporter.api.models.user;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class User {

    @SerializedName("id")
    public int id;

    @SerializedName("properties")
    public UserProperties properties;

    public static User createUser(int aId, UserProperties aProperties) {

        User newUser = new User();

        newUser.id = aId;
        newUser.properties = aProperties;

        return newUser;

    }

}
