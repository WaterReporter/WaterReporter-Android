package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 1/28/16.
 */
public final class UserOrgPatch {

    private UserOrgPatch() {
    }

    public static Map<String, Map> buildRequest (int orgId, String action) {

        //String op = selected ? "remove" : "add";

        Map<String, List<Map>> opListWrapper = new HashMap<String, List<Map>>();

        List<Map> opList = new ArrayList<>();

        Map<String, Integer> opObj = new HashMap<String, Integer>();

        opObj.put("id", orgId);

        opList.add(opObj);

        opListWrapper.put(action, opList);

        Map<String, Map> map = new HashMap<String, Map>();

        map.put("organization", opListWrapper);

        return map;

    }

}
