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

    // Notification settings

    @SerializedName("can_notify_admin_admin_closes_report_in_group")
    private boolean can_notify_admin_admin_closes_report_in_group;

    @SerializedName("can_notify_admin_admin_closes_report_in_territory")
    private boolean can_notify_admin_admin_closes_report_in_territory;

    @SerializedName("can_notify_admin_comment_on_report_in_group")
    private boolean can_notify_admin_comment_on_report_in_group;

    @SerializedName("can_notify_admin_comment_on_report_in_territory")
    private boolean can_notify_admin_comment_on_report_in_territory;

    @SerializedName("can_notify_admin_user_joins_group")
    private boolean can_notify_admin_user_joins_group;

    @SerializedName("can_notify_admin_user_submits_report_in_group")
    private boolean can_notify_admin_user_submits_report_in_group;

    @SerializedName("can_notify_admin_user_submits_report_in_territory")
    private boolean can_notify_admin_user_submits_report_in_territory;

    @SerializedName("can_notify_owner_admin_closes_owned_report")
    private boolean can_notify_owner_admin_closes_owned_report;

    @SerializedName("can_notify_owner_comment_on_owned_report")
    private boolean can_notify_owner_comment_on_owned_report;

    // Descriptive attributes, identifiers and related content

    @SerializedName("description")
    public String description;

    @SerializedName("first_name")
    public String first_name;

    @SerializedName("id")
    public int id;

    @SerializedName("images")
    public ArrayList<ReportPhoto> images;

    @SerializedName("last_name")
    public String last_name;

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

    public UserProperties(int aId, String aDescription, String aFirstName,
                          String aLastName, String aOrganizationName, String aPicture,
                          String aPublicEmail, String aTitle, ArrayList<ReportPhoto> aImages,
                          ArrayList<Organization> aOrganizations, ArrayList<Role> aRoles) {

        this.description = aDescription;
        this.first_name = aFirstName;
        this.id = aId;
        this.images = aImages;
        this.last_name = aLastName;
        this.organizations = aOrganizations;
        this.organization_name = aOrganizationName;
        this.picture = aPicture;
        this.public_email = aPublicEmail;
        this.roles = aRoles;
        this.title = aTitle;

    }

    public Map<String, String> getStringProperties() {

        Map<String, String> stringProperties = new HashMap<String, String>();

        stringProperties.put("description", this.description);
        stringProperties.put("first_name", this.first_name);
        stringProperties.put("last_name", this.last_name);
        stringProperties.put("organization_name", this.organization_name);
        stringProperties.put("picture", this.images.get(0).properties.icon_retina);
        stringProperties.put("public_email", this.public_email);
        stringProperties.put("title", this.title);

        return stringProperties;

    }

    public String[] getStringFields() {

        return new String[]{
                "description",
                "first_name",
                "last_name",
                "organization_name",
                "picture",
                "public_email",
                "title"
        };

    }

    public Map<String, Boolean> getNotificationProperties() {

        Map<String, Boolean> notificationSettings = new HashMap<>();

        notificationSettings.put("can_notify_admin_admin_closes_report_in_group", this.can_notify_admin_admin_closes_report_in_group);
        notificationSettings.put("can_notify_admin_admin_closes_report_in_territory", this.can_notify_admin_admin_closes_report_in_territory);
        notificationSettings.put("can_notify_admin_comment_on_report_in_group", this.can_notify_admin_comment_on_report_in_group);
        notificationSettings.put("can_notify_admin_comment_on_report_in_territory", this.can_notify_admin_comment_on_report_in_territory);
        notificationSettings.put("can_notify_admin_user_joins_group", this.can_notify_admin_user_joins_group);
        notificationSettings.put("can_notify_admin_user_submits_report_in_group", this.can_notify_admin_user_submits_report_in_group);
        notificationSettings.put("can_notify_admin_user_submits_report_in_territory", this.can_notify_admin_user_submits_report_in_territory);
        notificationSettings.put("can_notify_owner_admin_closes_owned_report", this.can_notify_owner_admin_closes_owned_report);
        notificationSettings.put("can_notify_owner_comment_on_owned_report", this.can_notify_owner_comment_on_owned_report);

        return notificationSettings;

    }

    public String[] getNotificationSettingFields() {

        return new String[]{
                "can_notify_owner_admin_closes_owned_report",
                "can_notify_owner_comment_on_owned_report"
        };

    }

    public String[] getAdminNotificationSettingFields() {

        return new String[]{
                "can_notify_owner_admin_closes_owned_report",
                "can_notify_owner_comment_on_owned_report",
                "can_notify_admin_admin_closes_report_in_group",
                "can_notify_admin_admin_closes_report_in_territory",
                "can_notify_admin_comment_on_report_in_group",
                "can_notify_admin_comment_on_report_in_territory",
                "can_notify_admin_user_joins_group",
                "can_notify_admin_user_submits_report_in_group",
                "can_notify_admin_user_submits_report_in_territory"
        };

    }

}
