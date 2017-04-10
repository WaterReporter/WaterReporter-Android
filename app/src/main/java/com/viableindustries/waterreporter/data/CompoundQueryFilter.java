package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 4/10/17.
 */

public class CompoundQueryFilter {

    @SerializedName("and")
    private List<QueryFilter> conditions;

    public CompoundQueryFilter(List aConditions) {

        this.conditions = aConditions;
    }
}
