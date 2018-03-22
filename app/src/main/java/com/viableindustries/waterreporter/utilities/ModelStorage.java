package com.viableindustries.waterreporter.utilities;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.viableindustries.waterreporter.api.models.campaign.Campaign;
import com.viableindustries.waterreporter.api.models.group.Group;
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

    public static User getStoredUser(SharedPreferences sharedPreferences, String key) {

        String storedUser = sharedPreferences.getString(key, "");

        return new Gson().fromJson(storedUser, User.class);

    }

    public static Organization getStoredOrganization(SharedPreferences sharedPreferences) {

        String storedOrganization = sharedPreferences.getString("stored_organization", "");

        return new Gson().fromJson(storedOrganization, Organization.class);

    }

    public static Group getStoredGroup(SharedPreferences sharedPreferences, String organizationId) {

        String storedGroup = sharedPreferences.getString(organizationId, "");

        return new Gson().fromJson(storedGroup, Group.class);

    }

    public static Campaign getStoredCampaign(SharedPreferences sharedPreferences) {

        String storedCampaign = sharedPreferences.getString("stored_campaign", "");

        return new Gson().fromJson(storedCampaign, Campaign.class);

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