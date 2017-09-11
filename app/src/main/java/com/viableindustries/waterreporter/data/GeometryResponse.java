package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Ryan Hamley on 10/7/14.
 * The GeometryResponse is the JSON object which contains the geometries of a GeoJSON object.
 */
public class GeometryResponse implements Serializable {

    @SerializedName("geometries")
    public final List<Geometry> geometries;

    @SerializedName("type")
    private final String type;

    public GeometryResponse(List<Geometry> geometriesList, String aType){

        this.geometries = geometriesList;

        this.type = aType;

    }

}
