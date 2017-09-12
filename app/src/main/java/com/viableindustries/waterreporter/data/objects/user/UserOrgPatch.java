package com.viableindustries.waterreporter.data.objects.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public class UserOrgPatch {

    private UserOrgPatch() {
    }

    public static Map<String, Map> buildRequest (int orgId, String action) {

        //String op = selected ? "remove" : "add";

        Map<String, List<Map>> opListWrapper = new HashMap<>();

        List<Map> opList = new ArrayList<>();

        Map<String, Integer> opObj = new HashMap<>();

        opObj.put("id", orgId);

        opList.add(opObj);

        opListWrapper.put(action, opList);

        Map<String, Map> map = new HashMap<>();

        map.put("organization", opListWrapper);

        return map;

    }

}
