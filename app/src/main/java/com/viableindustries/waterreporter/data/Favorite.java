package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 7/27/17.
 */

public class Favorite implements Serializable {

    @SerializedName("geometry")
    private final GeometryResponse geometry;

    @SerializedName("id")
    private final int id;

    @SerializedName("type")
    private final String type;

    @SerializedName("properties")
    public final FavoriteProperties properties;

    public Favorite(GeometryResponse aGeometry, int aID, String aType, FavoriteProperties aProperties) {
        this.geometry = aGeometry;
        this.id = aID;
        this.type = aType;
        this.properties = aProperties;
    }

}
