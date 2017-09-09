package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

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

    @SerializedName("status")
    public String status;

    public CommentPost (String aBody, List<Map<String, Integer>> aImages, int aReportId, String aReportState, String aStatus) {

        this.body = aBody;

        this.images = aImages;

        this.reportId = aReportId;

        this.reportState = aReportState;

        this.status = aStatus;

    }

}
