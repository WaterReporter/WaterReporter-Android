package com.viableindustries.waterreporter.data;

/**
 * Created by brendanmcintyre on 4/12/17.
 */

public class TagHolder {

    private static HashTag hashTag;

    public static HashTag getHashTag() {

        return hashTag;

    }

    public static void setHashTag(HashTag hashTag) {

        TagHolder.hashTag = hashTag;

    }

}
