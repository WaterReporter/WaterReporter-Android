package com.viableindustries.waterreporter.data;

/**
 * Created by Ryan Hamley on 10/9/14.
 * Similar to FeaturesResponse, but only returned when getting a single feature from the API.
 * The hierarchy is SingleFeatureResponse -> Report -> ReportPhoto && GeometryResponse -> Geometries
 */
public class SingleFeatureResponse {
    public Report response;
}
