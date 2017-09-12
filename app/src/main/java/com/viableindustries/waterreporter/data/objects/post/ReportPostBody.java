package com.viableindustries.waterreporter.data.objects.post;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.data.objects.GeometryResponse;
import com.viableindustries.waterreporter.data.objects.open_graph.OpenGraphProperties;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public class ReportPostBody implements Serializable {

    @SerializedName("images")
    public List<Map<String, Integer>> images;

    public ReportPostBody (GeometryResponse aGeometry, List<Map<String, Integer>> aGroups, List<Map<String, Integer>> aImages, boolean aIsPublic, String aReportDate,
                           String aDescription, String aState, List<OpenGraphProperties> aOpenGraph) {

        GeometryResponse geometry = aGeometry;

        List<Map<String, Integer>> groups = aGroups;

        this.images = aImages;

        boolean is_public = aIsPublic;

        String report_date = aReportDate;

        String description = aDescription;

        String state = aState;

        List<OpenGraphProperties> social = aOpenGraph;

    }

}
