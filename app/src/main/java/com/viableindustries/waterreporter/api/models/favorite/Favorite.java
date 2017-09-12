package com.viableindustries.waterreporter.api.models.favorite;


import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.geometry.GeometryResponse;

/**
 * Created by brendanmcintyre on 7/27/17.
 */

public class Favorite {

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
