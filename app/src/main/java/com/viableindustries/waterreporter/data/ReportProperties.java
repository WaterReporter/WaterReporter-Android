package com.viableindustries.waterreporter.data;

import android.widget.ListView;

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
    public List<Comment> comments;

    @SerializedName("created")
    public String created;

    @SerializedName("groups")
    public ArrayList<Organization> groups;

    @SerializedName("id")
    public int id;

    @SerializedName("images")
    public List<ReportPhoto> images;

    @SerializedName("is_featured")
    public boolean is_featured;

    @SerializedName("is_public")
    public boolean is_public;

    @SerializedName("likes")
    public List<Favorite> favorites;

    @SerializedName("owner")
    public User owner;

    @SerializedName("owner_id")
    public int owner_id;

    @SerializedName("report_date")
    public String report_date;

    @SerializedName("report_description")
    public String report_description;

    @SerializedName("social")
    public List<OpenGraphObject> open_graph;

    @SerializedName("state")
    public String state;

    @SerializedName("tags")
    public List<HashTag> tags;

    @SerializedName("territory")
    public Territory territory;

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
