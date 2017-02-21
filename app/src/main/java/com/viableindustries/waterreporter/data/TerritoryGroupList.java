package com.viableindustries.waterreporter.data;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 2/21/17.
 */

public class TerritoryGroupList {

    private static ArrayList<Organization> groupList;

    public static ArrayList<Organization> getList() {

        return groupList;

    }

    public static void setList(ArrayList<Organization> groupList) {

        TerritoryGroupList.groupList = groupList;

    }

}
