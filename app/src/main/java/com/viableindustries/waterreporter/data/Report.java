package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.UtilityMethods;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Ryan Hamley on 10/6/14.
 * Report class used to accept JSON responses from API. A List of Reports is accepted
 * by FeaturesResponse.
 */
public class Report implements Serializable {
    @SerializedName("attachment_76fc17d6574c401d9a20d18187f8083e")
    public List<ReportPhoto> photo;
    public String comments;
    public String created;
    public String date;
    public GeometryResponse geometry;
    public int id;
    @SerializedName("is_a_pollution_report?")
    public boolean isPollution;
    public String status;
    @SerializedName("type_05a300e835024771a51a6d3114e82abc")
    public List<Relationship> pollution;
    @SerializedName("type_0e9423a9a393481f82c4f22ff5954567")
    public List<Relationship> activity;
    @SerializedName("type_8f432efc18c545ea9578b4bdea860b4c")
    public List<Relationship> type;
    public String updated;
    @SerializedName("useremail_address")
    public String email;
    public String username;
    public String usertitle;


    public String getFormattedDateString(){
        String[] parts = date.split("-");

        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);
        int year = Integer.parseInt(parts[0]);

        UtilityMethods utilityMethods = new UtilityMethods();

        return utilityMethods.getDateString(month, day, year);
    }
}
