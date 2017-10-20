package com.viableindustries.waterreporter.api.models.user;

import com.viableindustries.waterreporter.api.models.group.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brendanmcintyre on 8/22/16.
 */

public class UserGroupList {

    private static List<Group> groupList;

    public static List<Group> getList() {

        return groupList;

    }

    public static void setList(List<Group> aGroupList) {

        UserGroupList.groupList = aGroupList;

    }

}
