package com.viableindustries.waterreporter.data.objects.favorite;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.data.objects.GeometryResponse;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 7/27/17.
 */

public class Favorite implements Serializable {

    @SerializedName("properties")
    public FavoriteProperties properties;

    public Favorite(GeometryResponse aGeometry, int aID, String aType, FavoriteProperties aProperties) {
        GeometryResponse geometry = aGeometry;
        int id = aID;
        String type = aType;
        this.properties = aProperties;
    }

}
