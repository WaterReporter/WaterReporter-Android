package com.viableindustries.waterreporter.api.models.post;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.geometry.GeometryResponse;

/**
 * Created by Ryan Hamley on 10/6/14.
 * Report class used to accept JSON responses from stg.api. A List of Reports is accepted
 * by FeaturesResponse.
 */
public class Report {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("properties")
    public ReportProperties properties;

    @SerializedName("type")
    public String type;

}
