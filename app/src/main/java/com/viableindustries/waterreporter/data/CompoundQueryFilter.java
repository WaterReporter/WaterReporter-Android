package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 4/10/17.
 */

class CompoundQueryFilter {

    @SerializedName("and")
    private final List<QueryFilter> conditions;

    public CompoundQueryFilter(List<QueryFilter> aConditions) {

        this.conditions = aConditions;
    }
}
