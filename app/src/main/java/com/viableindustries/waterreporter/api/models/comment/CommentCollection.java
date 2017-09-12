package com.viableindustries.waterreporter.api.models.comment;


import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.CollectionProperties;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public class CommentCollection {

    @SerializedName("features")
    private ArrayList<Comment> features;

    @SerializedName("properties")
    private CollectionProperties properties;

    @SerializedName("type")
    private String type;

    public ArrayList<Comment> getFeatures() {

        return features;

    }

}
