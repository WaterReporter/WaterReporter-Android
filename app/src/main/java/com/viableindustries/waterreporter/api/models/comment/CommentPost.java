package com.viableindustries.waterreporter.api.models.comment;

import com.google.gson.annotations.SerializedName;
import com.viableindustries.waterreporter.api.models.open_graph.OpenGraphProperties;

import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public class CommentPost {

    @SerializedName("body")
    public String body;

    @SerializedName("images")
    public List<Map<String, Integer>> images;

    @SerializedName("report_id")
    public int reportId;

    @SerializedName("report_state")
    public String reportState;

    @SerializedName("social")
    public List<OpenGraphProperties> social;

    @SerializedName("status")
    public String status;

    public CommentPost(
            String aBody,
            List<Map<String, Integer>> aImages,
            int aReportId,
            String aReportState,
            List<OpenGraphProperties> aSocial,
            String aStatus) {

        this.body = aBody;

        this.images = aImages;

        this.reportId = aReportId;

        this.reportState = aReportState;

        this.social = aSocial;

        this.status = aStatus;

    }

}
