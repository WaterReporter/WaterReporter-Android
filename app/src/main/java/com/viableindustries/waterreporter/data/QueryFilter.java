package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/3/16.
 */
public class QueryFilter {

    @SerializedName("name")
    String name;

    @SerializedName("op")
    String op;

    @SerializedName("val")
    Object val;

    public QueryFilter (String aName, String aOp, Object aVal) {

        this.name = aName;

        this.op = aOp;

        this.val = aVal;

    }

}
