package com.viableindustries.waterreporter.api.models.geometry;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ryan Hamley on 10/7/14.
 * The GeometryResponse is the JSON object which contains the geometries of a GeoJSON object.
 */
public class GeometryResponse {

    @SerializedName("geometries")
    public List<Geometry> geometries;

    @SerializedName("type")
    public String type;

    public GeometryResponse(List<Geometry> geometriesList, String aType){

        this.geometries = geometriesList;

        this.type = aType;

    }

}
