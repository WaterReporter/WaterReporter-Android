package com.viableindustries.waterreporter.data;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 4/13/17.
 */

class TagGroupList {

    private static ArrayList<Organization> groupList;

    public static ArrayList<Organization> getList() {

        return groupList;

    }

    public static void setList(ArrayList<Organization> groupList) {

        TagGroupList.groupList = groupList;

    }

}
