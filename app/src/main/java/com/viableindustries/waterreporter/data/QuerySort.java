package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 3/3/16.
 */
public class QuerySort {

    @SerializedName("field")
    String field;

    @SerializedName("direction")
    String direction;

    public QuerySort (String aField, String aDirection) {

        this.field = aField;

        this.direction = aDirection;

    }

}