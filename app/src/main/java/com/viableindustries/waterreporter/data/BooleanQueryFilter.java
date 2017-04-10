package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 4/10/17.
 */

public class BooleanQueryFilter {

    @SerializedName("or")
    private List<QueryFilter> conditions;

    public BooleanQueryFilter(List aConditions) {

        this.conditions = aConditions;
    }
}
