package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public class ReportPostBody implements Serializable {

    @SerializedName("geometry")
    public GeometryResponse geometry;

    @SerializedName("groups")
    public List<Map<String, Integer>> groups;

    @SerializedName("images")
    public List<Map<String, Integer>> images;

    @SerializedName("is_public")
    public boolean is_public;

    @SerializedName("report_date")
    public String report_date;

    @SerializedName("report_description")
    public String description;

    @SerializedName("social")
    public List<OpenGraphProperties> social;

    @SerializedName("state")
    public String state;

    public ReportPostBody (GeometryResponse aGeometry, List<Map<String, Integer>> aGroups, List<Map<String, Integer>> aImages, boolean aIsPublic, String aReportDate,
                           String aDescription, String aState, List<OpenGraphProperties> aOpenGraph) {

        this.geometry = aGeometry;

        this.groups = aGroups;

        this.images = aImages;

        this.is_public = aIsPublic;

        this.report_date = aReportDate;

        this.description = aDescription;

        this.state = aState;

        this.social = aOpenGraph;

    }

}
