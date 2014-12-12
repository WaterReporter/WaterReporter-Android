package com.viableindustries.waterreporter.data;

import com.orm.SugarRecord;

/**
 * Created by Ryan Hamley on 10/28/14.
 * This class turns user reports into objects which can be saved to the app database with Sugar
 */
public class Submission extends SugarRecord<Submission> {
    public String type;
    public String date;
    public String activity;
    public String comments;
    public String photoPath;
    public double latitude;
    public double longitude;
    public int feature_id;


    public Submission(){

    }

    public Submission(String aType, String aDate, String anActivity, String aComment, double aLatitude,
                      double aLongitude, String aPhotoPath){
        this.type = aType;
        this.date = aDate;
        this.activity = anActivity;
        this.comments = aComment;
        this.latitude = aLatitude;
        this.longitude = aLongitude;
        this.photoPath = aPhotoPath;
    }
}