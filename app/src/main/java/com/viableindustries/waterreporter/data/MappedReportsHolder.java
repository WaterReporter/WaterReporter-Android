package com.viableindustries.waterreporter.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brendanmcintyre on 11/11/16.
 */

public class MappedReportsHolder {

    //private static List<Report> reports;

    private static final Map<String, Report> reports = new HashMap<>();

    public Report getReport(String key) {

        return reports.get(key);

    }

    public void addReport(String key, Report report) {

        MappedReportsHolder.reports.put(key, report);

    }

}
