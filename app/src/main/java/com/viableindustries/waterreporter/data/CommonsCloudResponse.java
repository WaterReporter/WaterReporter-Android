package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ryan Hamley on 10/4/14.
 * Defines the initial JSON response from the API for use with Retrofit
 * The hierarchy is CommonsCloudResponse -> FeaturesResponse -> Report
 *     -> ReportPhoto && GeometryResponse -> Geometries
 */
public class CommonsCloudResponse {
    @SerializedName("response")
    public FeaturesResponse featuresResponse;
}
