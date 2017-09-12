package com.viableindustries.waterreporter.map_box;

import com.viableindustries.waterreporter.api.models.post.Report;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brendanmcintyre on 11/11/16.
 */

public class MappedReportsHolder {

    private static final Map<String, Report> reports = new HashMap<>();

    public Report getReport(String key) {

        return reports.get(key);

    }

    public void addReport(String key, Report report) {

        MappedReportsHolder.reports.put(key, report);

    }

}
