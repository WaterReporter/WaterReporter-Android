package com.viableindustries.waterreporter.api.models.organization;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 4/13/17.
 */

public class GroupListHolder {

    private static ArrayList<Organization> groupList;

    public static ArrayList<Organization> getList() {

        return groupList;

    }

    public static void setList(ArrayList<Organization> groupList) {

        GroupListHolder.groupList = groupList;

    }

}