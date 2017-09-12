package com.viableindustries.waterreporter.api.models.post;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.geometry.GeometryResponse;
import com.viableindustries.waterreporter.api.models.image.ImageProperties;

/**
 * Created by Ryan Hamley on 10/9/14.
 * ReportPhoto is the photo information returned with a report by the stg.api.
 */
public class ReportPhoto {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("properties")
    public ImageProperties properties;

    @SerializedName("type")
    public String type;

}
