package com.viableindustries.waterreporter.mbox;

import com.mapbox.mapboxsdk.annotations.BaseMarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.MarkerView;

/**
 * Created by brendanmcintyre on 8/3/16.
 */

public class CustomMarkerView extends MarkerView {

//    private Report report;

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
    private int inFocus;
    private int index;

    public CustomMarkerView(BaseMarkerViewOptions baseMarkerViewOptions,
                            int reportId, String reportDescription, String thumbNail, String fullImage,
                            String creationDate, String watershedName, String groupList, String commentCount,
                            String userName, String userAvatar, String status, int inFocus, int idx) {
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
        this.inFocus = inFocus;
        this.index = idx;
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

    public int isInFocus() { return inFocus; }

    public int getIndex() { return index; }

}
