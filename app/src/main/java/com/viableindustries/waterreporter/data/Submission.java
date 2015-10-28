package com.viableindustries.waterreporter.data;

import com.orm.SugarRecord;

/**
 * Created by Ryan Hamley on 10/28/14.
 * This class turns user reports into objects which can be saved to the app database with Sugar
 */
public class Submission extends SugarRecord<Submission> {

    public String report_date;

    public String report_description;

    public String photoUrl;

    public double latitude;

    public double longitude;

    public int feature_id;

    public Submission(){

    }

    public Submission(String aDate, String aDescription, double aLatitude,
                      double aLongitude, String aPhotoUrl){

        this.report_date = aDate;

        this.report_description = aDescription;

        this.latitude = aLatitude;

        this.longitude = aLongitude;

        this.photoUrl = aPhotoUrl;

    }

}