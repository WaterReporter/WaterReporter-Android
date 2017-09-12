package com.viableindustries.waterreporter.data.objects.comment;

import java.util.List;
import java.util.Map;

/**
 * Created by brendanmcintyre on 8/27/15.
 */
public class CommentPost {

    public CommentPost (String aBody, List<Map<String, Integer>> aImages, int aReportId, String aReportState, String aStatus) {

        String body = aBody;

        List<Map<String, Integer>> images = aImages;

        int reportId = aReportId;

        String reportState = aReportState;

        String status = aStatus;

    }

}
