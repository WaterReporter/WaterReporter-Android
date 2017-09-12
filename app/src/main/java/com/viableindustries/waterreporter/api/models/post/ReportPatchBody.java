package com.viableindustries.waterreporter.api.models.post;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.geometry.GeometryResponse;

import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 11/14/16.
 */

public class ReportPatchBody {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("groups")
    public List<Map<String, Integer>> groups;

    @SerializedName("report_date")
    public String report_date;

    @SerializedName("report_description")
    public String description;

    @SerializedName("state")
    public String state;

    public ReportPatchBody (GeometryResponse aGeometry, List<Map<String, Integer>> aGroups, String aReportDate,
                            String aDescription, String aState) {

        this.geometry = aGeometry;

        this.groups = aGroups;

        this.report_date = aReportDate;

        this.description = aDescription;

        this.state = aState;

    }

}
