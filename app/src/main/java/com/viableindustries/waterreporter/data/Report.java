package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Ryan Hamley on 10/6/14.
 * Report class used to accept JSON responses from stg.api. A List of Reports is accepted
 * by FeaturesResponse.
 */
public class Report implements Serializable {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("properties")
    public ReportProperties properties;

    @SerializedName("type")
    public String type;

}
