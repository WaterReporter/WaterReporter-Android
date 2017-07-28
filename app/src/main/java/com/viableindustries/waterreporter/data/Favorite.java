package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 7/27/17.
 */

public class Favorite implements Serializable {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("type")
    public String type;

    @SerializedName("properties")
    public FavoriteProperties properties;

    public Favorite(GeometryResponse aGeometry, int aID, String aType, FavoriteProperties aProperties) {
        this.geometry = aGeometry;
        this.id = aID;
        this.type = aType;
        this.properties = aProperties;
    }

}
