package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by brendanmcintyre on 7/26/17.
 */

public class OpenGraphProperties implements Serializable {

    //
    // Creation time
    //

    @SerializedName("created")
    private String created;

    //
    // Description
    //

    @SerializedName("og_description")
    private String description;

    //
    // Image
    //

    @SerializedName("og_image_url")
    private String imageUrl;

    @SerializedName("og_image_secure_url")
    private String imageSecureUrl;

    @SerializedName("og_image_type")
    private String imageType;

    @SerializedName("og_image_width")
    private String imageWidth;

    @SerializedName("og_image_height")
    private String imageHeight;

    //
    // Published time
    //

    @SerializedName("og_published_time")
    private String publishedTime;

    //
    // Title
    //

    @SerializedName("og_title")
    private String openGraphTitle;

    //
    // Type
    //

    @SerializedName("og_type")
    private String type;

    //
    // URL
    //

    @SerializedName("og_url")
    private String url;

    //
    // Relations
    //

    // Owner relation ID

    @SerializedName("owner_id")
    private int ownerId;

    public OpenGraphProperties () {}

}