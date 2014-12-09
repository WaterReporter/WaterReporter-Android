package com.viableindustries.waterreporter.data;

import com.orm.SugarRecord;

/**
 * Created by Ryan Hamley on 10/28/14.
 * This class turns user reports into objects which can be saved to the app database with Sugar
 */
public class Submission extends SugarRecord<Submission> {
    public String date;
    public String location;
    public String issue;
    public String facility;
    public String comments;
    public String photoPath;
    public double latitude;
    public double longitude;
    public int feature_id;


    public Submission(){

    }

    public Submission(String aDate, String aLocation, String anIssue,
                      String aFacility, String aComment, double aLatitude, double aLongitude,
                      String aPhotoPath){
        this.date = aDate;
        this.location = aLocation;
        this.issue = anIssue;
        this.facility = aFacility;
        this.comments = aComment;
        this.latitude = aLatitude;
        this.longitude = aLongitude;
        this.photoPath = aPhotoPath;
    }
}