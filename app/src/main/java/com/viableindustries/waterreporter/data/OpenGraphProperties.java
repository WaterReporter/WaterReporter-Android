package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 7/26/17.
 */

public class OpenGraphProperties implements Serializable {

    //
    // Creation time
    //

    @SerializedName("created")
    public String created;

    //
    // Description
    //

    @SerializedName("og_description")
    public String description;

    //
    // Image
    //

    @SerializedName("og_image_url")
    public String imageUrl;

    @SerializedName("og_image_secure_url")
    public String imageSecureUrl;

    @SerializedName("og_image_type")
    public String imageType;

    @SerializedName("og_image_width")
    public String imageWidth;

    @SerializedName("og_image_height")
    public String imageHeight;

    //
    // Published time
    //

    @SerializedName("og_published_time")
    public String publishedTime;

    //
    // Title
    //

    @SerializedName("og_title")
    public String openGraphTitle;

    //
    // Type
    //

    @SerializedName("og_type")
    public String type;

    //
    // URL
    //

    @SerializedName("og_url")
    public String url;

    //
    // Relations
    //

    // Owner relation ID

    @SerializedName("owner_id")
    public int ownerId;

    public OpenGraphProperties(String aImageUrl, String aDescription, String aTitle, String aUrl, int aOwnerId) {

        this.description = aDescription;
        this.openGraphTitle = aTitle;
        this.url = aUrl;
        this.imageUrl = aImageUrl;
        this.ownerId = aOwnerId;

    }

}