package com.viableindustries.waterreporter.utilities;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.api.models.hashtag.HashTag;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.territory.Territory;
import com.viableindustries.waterreporter.api.models.user.User;

/**
 * Created by brendanmcintyre on 9/14/17.
 */

public class ModelStorage {

    private ModelStorage() {
    }

    public static Report getStoredPost(SharedPreferences sharedPreferences) {

        String storedPost = sharedPreferences.getString("stored_post", "");

        return new Gson().fromJson(storedPost, Report.class);

    }

    public static User getStoredUser(SharedPreferences sharedPreferences) {

        String storedUser = sharedPreferences.getString("stored_user", "");

        return new Gson().fromJson(storedUser, User.class);

    }

    public static Organization getStoredGroup(SharedPreferences sharedPreferences) {

        String storedGroup = sharedPreferences.getString("stored_group", "");

        return new Gson().fromJson(storedGroup, Organization.class);

    }

    public static Territory getStoredTerritory(SharedPreferences sharedPreferences) {

        String storedTerritory = sharedPreferences.getString("stored_territory", "");

        return new Gson().fromJson(storedTerritory, Territory.class);

    }

    public static HashTag getStoredHashtag(SharedPreferences sharedPreferences) {

        String storedHashtag = sharedPreferences.getString("stored_hashtag", "");

        return new Gson().fromJson(storedHashtag, HashTag.class);

    }

    public static void storeModel(SharedPreferences sharedPreferences, Object model, String key) {

        sharedPreferences.edit().putString(key, new Gson().toJson(model)).apply();

    }

    public static void removeModel(SharedPreferences sharedPreferences, String key) {

        sharedPreferences.edit().remove(key).apply();

    }

}