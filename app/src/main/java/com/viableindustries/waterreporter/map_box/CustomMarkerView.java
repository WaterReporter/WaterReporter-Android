package com.viableindustries.waterreporter.map_box;

import com.mapbox.mapboxsdk.annotations.BaseMarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.MarkerView;

/**
 * Created by brendanmcintyre on 8/3/16.
 */

public class CustomMarkerView extends MarkerView {

//    private Report report;

    private final int reportId;
    private final String reportDescription;
    private final String thumbNail;
    private final String fullImage;
    private final String creationDate;
    private final String watershedName;
    private final String groupList;
    private final String commentCount;
    private final String userName;
    private final String userAvatar;
    private final String status;
    private final int inFocus;
    private final int index;

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
