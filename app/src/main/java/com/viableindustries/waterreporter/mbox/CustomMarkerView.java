package com.viableindustries.waterreporter.mbox;

import com.mapbox.mapboxsdk.annotations.BaseMarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.MarkerView;

import java.util.List;

/**
 * Created by brendanmcintyre on 8/3/16.
 */

public class CustomMarkerView extends MarkerView {

    private int reportId;
    private String reportDescription;
    private String thumbNail;
    private String fullImage;
    private String creationDate;
    private String watershedName;
    private String groupList;
    private String commentCount;
    private String userName;
    private String userAvatar;
    private String status;

    public CustomMarkerView(BaseMarkerViewOptions baseMarkerViewOptions,
                            int reportId, String reportDescription, String thumbNail, String fullImage,
                            String creationDate, String watershedName, String groupList, String commentCount,
                            String userName, String userAvatar, String status) {
        super(baseMarkerViewOptions);
        this.thumbNail = thumbNail;
        this.fullImage = fullImage;
        this.reportId = reportId;
        this.reportDescription = reportDescription;
        this.creationDate = creationDate;
        this.watershedName = watershedName;
        this.groupList = groupList;
        this.commentCount = commentCount;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.status = status;
    }

    public String getThumbNail() {
        return thumbNail;
    }

    public String getFullImage() {
        return fullImage;
    }

    public int getReportId() {
        return reportId;
    }

    public String getReportDescription() {
        return reportDescription;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getWatershedName() {
        return watershedName;
    }

    public String getGroupList() {
        return groupList;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public String getStatus() { return status; }

}
