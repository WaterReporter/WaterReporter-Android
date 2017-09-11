package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 4/10/17.
 */

public class BooleanQueryFilter {

    @SerializedName("or")
    private final List<Object> conditions;

    public BooleanQueryFilter(List<Object> aConditions) {

        this.conditions = aConditions;
    }

}
