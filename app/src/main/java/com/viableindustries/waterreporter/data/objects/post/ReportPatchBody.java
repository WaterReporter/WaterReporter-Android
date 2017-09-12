package com.viableindustries.waterreporter.data.objects.post;

import com.viableindustries.waterreporter.data.objects.GeometryResponse;

import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 11/14/16.
 */

public class ReportPatchBody {

    public ReportPatchBody (GeometryResponse aGeometry, List<Map<String, Integer>> aGroups, String aReportDate,
                            String aDescription, String aState) {

        GeometryResponse geometry = aGeometry;

        List<Map<String, Integer>> groups = aGroups;

        String report_date = aReportDate;

        String description = aDescription;

        String state = aState;

    }

}
