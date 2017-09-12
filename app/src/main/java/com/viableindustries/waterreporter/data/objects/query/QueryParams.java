package com.viableindustries.waterreporter.data.objects.query;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/3/16.
 */
public class QueryParams {

    public QueryParams (List<Object> aFilters, List<QuerySort> aOrderBy) {

        List<Object> filters = aFilters;

        List<QuerySort> order_by = aOrderBy;

    }

}