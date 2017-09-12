package com.viableindustries.waterreporter.data.objects.notification;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by brendanmcintyre on 11/15/16.
 */

public class NotificationPatchBody {

    @SerializedName("settings")
    public Map<String, Boolean> settings;

    public NotificationPatchBody (Map<String, Boolean> notificationSettings) {

        this.settings = notificationSettings;

    }

}
