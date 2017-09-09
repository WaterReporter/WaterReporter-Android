package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 3/3/16.
 */
public class QueryFilter {

    @SerializedName("name")
    private String name;

    @SerializedName("op")
    private String op;

    @SerializedName("val")
    private Object val;

    public QueryFilter (String aName, String aOp, Object aVal) {

        this.name = aName;

        this.op = aOp;

        this.val = aVal;

    }

}
