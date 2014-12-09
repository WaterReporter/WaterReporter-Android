package com.viableindustries.waterreporter.data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Ryan Hamley on 10/7/14.
 * The GeometryResponse is the JSON object which contains the geometries of a GeoJSON object.
 */
public class GeometryResponse implements Serializable{
    public List<Geometries> geometries;
    public String type;

    public GeometryResponse(List<Geometries> geometriesList, String aType){
        this.geometries = geometriesList;
        this.type = aType;
    }
}
