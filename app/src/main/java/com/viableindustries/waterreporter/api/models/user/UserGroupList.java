package com.viableindustries.waterreporter.api.models.user;

import com.viableindustries.waterreporter.api.models.group.Group;

import java.util.ArrayList;

/**
 * Created by brendanmcintyre on 8/22/16.
 */

public class UserGroupList {

    private static ArrayList<Group> groupList;

    public static ArrayList<Group> getList() {

        return groupList;

    }

    public static void setList(ArrayList<Group> aGroupList) {

        UserGroupList.groupList = aGroupList;

    }

}
