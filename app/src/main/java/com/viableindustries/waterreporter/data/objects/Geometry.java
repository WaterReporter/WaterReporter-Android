package com.viableindustries.waterreporter.data.objects;

import com.google.gson.annotations.SerializedName;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Ryan Hamley on 10/7/14.
 * The Geometry class is the JSON object of type GeometryCollection which contains coordinates.
 */
public class Geometry implements Serializable {

    @SerializedName("coordinates")
    public ArrayList<Double> coordinates;

    public Geometry(ArrayList<Double> aList, String aType){

        this.coordinates = aList;

        String type = aType;

    }

    public LatLng getCoordinates(){
        return new LatLng(coordinates.get(1), coordinates.get(0));
    }

}
