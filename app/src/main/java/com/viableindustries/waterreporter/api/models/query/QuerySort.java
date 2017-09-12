package com.viableindustries.waterreporter.api.models.query;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 3/3/16.
 */
public class QuerySort {

    @SerializedName("field")
    private String field;

    @SerializedName("direction")
    private String direction;

    public QuerySort(String aField, String aDirection) {

        this.field = aField;

        this.direction = aDirection;

    }

}
