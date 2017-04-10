package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/3/16.
 */
public class QueryParams {

    @SerializedName("filters")
    private List<Object> filters;

    @SerializedName("order_by")
    private List<QuerySort> order_by;

    public QueryParams (List<Object> aFilters, List<QuerySort> aOrderBy) {

        this.filters = aFilters;

        this.order_by = aOrderBy;

    }

}