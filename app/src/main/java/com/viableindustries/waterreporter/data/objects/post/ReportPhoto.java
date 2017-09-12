package com.viableindustries.waterreporter.data.objects.post;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.data.objects.GeometryResponse;
import com.viableindustries.waterreporter.data.objects.image.ImageProperties;

import java.io.Serializable;

/**
 * Created by Ryan Hamley on 10/9/14.
 * ReportPhoto is the photo information returned with a report by the stg.api.
 */
public class ReportPhoto implements Serializable {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("id")
    public int id;

    @SerializedName("properties")
    public ImageProperties properties;

    @SerializedName("type")
    public String type;

}
