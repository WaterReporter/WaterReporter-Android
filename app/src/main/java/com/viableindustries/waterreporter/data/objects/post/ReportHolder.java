package com.viableindustries.waterreporter.data.objects.post;

/**
 * Created by brendanmcintyre on 8/30/16.
 */

public class ReportHolder {

    private static Report report;

    public static Report getReport() {

        return report;

    }

    public static void setReport(Report report) {

        ReportHolder.report = report;

    }

}
