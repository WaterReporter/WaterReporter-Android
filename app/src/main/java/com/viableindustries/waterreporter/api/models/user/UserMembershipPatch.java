package com.viableindustries.waterreporter.api.models.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public final class UserMembershipPatch {

    private UserMembershipPatch() {
    }

    public static Map<String, List> buildRequest(int orgId) {

        Map<String, List> requestData = new HashMap<>();
        
        // Set up organization object

//        Map<String, List<Map>> organizationListWrapper = new HashMap<>();

        List<Map> organizationList = new ArrayList<>();

        Map<String, Integer> organizationObj = new HashMap<>();

        organizationObj.put("id", orgId);

        organizationList.add(organizationObj);

//        organizationListWrapper.put(action, organizationList);

        requestData.put("organization", organizationList);

        // Set up group object

//        Map<String, List<Map>> groupListWrapper = new HashMap<>();

        List<Map> groupList = new ArrayList<>();

        Map<String, Integer> groupObj = new HashMap<>();

        groupObj.put("organization_id", orgId);

        groupList.add(groupObj);

//        groupListWrapper.put(action, groupList);

        requestData.put("groups", groupList);

        return requestData;

    }

}
