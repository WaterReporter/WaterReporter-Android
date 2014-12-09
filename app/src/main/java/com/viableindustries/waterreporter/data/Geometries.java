package com.viableindustries.waterreporter.data;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Ryan Hamley on 10/7/14.
 * The Geometries class is the JSON object of type GeometryCollection which contains coordinates.
 */
public class Geometries implements Serializable{
    public ArrayList<Float> coordinates;
    public String type;

    public Geometries(ArrayList<Float> aList, String aType){
        this.coordinates = aList;
        this.type = aType;
    }

    public LatLng getCoordinates(){
        return new LatLng(coordinates.get(1), coordinates.get(0));
    }
}
