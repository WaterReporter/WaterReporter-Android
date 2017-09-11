package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 11/14/16.
 */

public class ReportPatchBody {

    @SerializedName("geometry")
    private final GeometryResponse geometry;

    @SerializedName("groups")
    private final List<Map<String, Integer>> groups;

    @SerializedName("report_date")
    private final String report_date;

    @SerializedName("report_description")
    private final String description;

    @SerializedName("state")
    private final String state;

    public ReportPatchBody (GeometryResponse aGeometry, List<Map<String, Integer>> aGroups, String aReportDate,
                           String aDescription, String aState) {

        this.geometry = aGeometry;

        this.groups = aGroups;

        this.report_date = aReportDate;

        this.description = aDescription;

        this.state = aState;

    }

}
