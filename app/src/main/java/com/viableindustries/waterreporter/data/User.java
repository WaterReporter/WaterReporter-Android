package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class User implements Serializable {

    @SerializedName("id")
    public int id;

    @SerializedName("properties")
    public UserProperties properties;

    public static User createUser(int aId, UserProperties aProperties){

        User newUser = new User();

        newUser.id = aId;
        newUser.properties = aProperties;

        return newUser;

    }

}
