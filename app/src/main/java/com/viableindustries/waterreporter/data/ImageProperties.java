package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by brendanmcintyre on 8/26/15.
 */
public class ImageProperties implements Serializable {

    @SerializedName("created")
    public String created;

    @SerializedName("credit")
    public String credit;

    @SerializedName("credit_link")
    public String credit_link;

    @SerializedName("filename")
    public String filename;

    @SerializedName("filesize")
    public String filesize;

    @SerializedName("filetype")
    public String filetype;

    @SerializedName("icon")
    public String icon;

    @SerializedName("icon_retina")
    public String icon_retina;

    @SerializedName("id")
    public int id;

    @SerializedName("original")
    public String original;

    @SerializedName("owner_id")
    public int owner_id;

    @SerializedName("square")
    public String square;

    @SerializedName("square_retina")
    public String square_retina;

    @SerializedName("thumbnail")
    public String thumbnail;

    @SerializedName("thumbnail_retina")
    public String thumbnail_retina;

    @SerializedName("updated")
    public String updated;

}
