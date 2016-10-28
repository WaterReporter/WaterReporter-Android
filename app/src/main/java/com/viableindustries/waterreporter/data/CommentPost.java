package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public class CommentPost {

    @SerializedName("body")
    public String body;

    @SerializedName("report_id")
    public int reportId;

    @SerializedName("report_state")
    public String reportState;

    @SerializedName("status")
    public String status;

    public CommentPost (String aBody, int aReportId, String aReportState, String aStatus) {

        this.body = aBody;

        this.reportId = aReportId;

        this.reportState = aReportState;

        this.status = aStatus;

    }

}
