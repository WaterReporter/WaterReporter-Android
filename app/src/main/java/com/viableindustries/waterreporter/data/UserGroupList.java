package com.viableindustries.waterreporter.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brendanmcintyre on 8/22/16.
 */

public class UserGroupList {

    private static ArrayList<Organization> organizationList;

    public static ArrayList<Organization> getList() {

        return organizationList;

    }

    public static void setList(ArrayList<Organization> organizationList) {

        UserGroupList.organizationList = organizationList;

    }

}
