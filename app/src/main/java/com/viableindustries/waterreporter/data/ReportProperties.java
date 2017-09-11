package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class ReportProperties implements Serializable {

    @SerializedName("closed_by")
    public User closed_by;

    @SerializedName("closed_on")
    public String closed_on;

    @SerializedName("comments")
    public final List<Comment> comments;

    @SerializedName("created")
    public final String created;

    @SerializedName("groups")
    public final ArrayList<Organization> groups;

    @SerializedName("id")
    public int id;

    @SerializedName("images")
    public final List<ReportPhoto> images;

    @SerializedName("is_featured")
    public boolean is_featured;

    @SerializedName("is_public")
    public boolean is_public;

    @SerializedName("likes")
    public final List<Favorite> favorites;

    @SerializedName("owner")
    public final User owner;

    @SerializedName("owner_id")
    public final int owner_id;

    @SerializedName("report_date")
    private String report_date;

    @SerializedName("report_description")
    public final String description;

    @SerializedName("social")
    public final List<OpenGraphObject> open_graph;

    @SerializedName("state")
    public final String state;

    @SerializedName("tags")
    public List<HashTag> tags;

    @SerializedName("territory")
    public final Territory territory;

    @SerializedName("territory_id")
    public int territory_id;

    @SerializedName("updated")
    public String updated;

    public String getFormattedDateString() {

        try {

            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

            Date date = iso.parse(report_date);

            SimpleDateFormat sdfOutput = new SimpleDateFormat("MMM d, yyyy", Locale.US);

            report_date = sdfOutput.format(date);

        } catch (ParseException pe) {

            System.out.println("Parse Exception : " + pe);

        }

        return report_date.replace("AM", "am").replace("PM", "pm");

    }

}
