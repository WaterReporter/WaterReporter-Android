package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public class CommentPost {

    @SerializedName("body")
    private final String body;

    @SerializedName("images")
    private final List<Map<String, Integer>> images;

    @SerializedName("report_id")
    private final int reportId;

    @SerializedName("report_state")
    private final String reportState;

    @SerializedName("status")
    private final String status;

    public CommentPost (String aBody, List<Map<String, Integer>> aImages, int aReportId, String aReportState, String aStatus) {

        this.body = aBody;

        this.images = aImages;

        this.reportId = aReportId;

        this.reportState = aReportState;

        this.status = aStatus;

    }

}
