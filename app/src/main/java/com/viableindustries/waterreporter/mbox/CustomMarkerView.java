package com.viableindustries.waterreporter.mbox;

import com.mapbox.mapboxsdk.annotations.BaseMarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.MarkerView;

/**
 * Created by brendanmcintyre on 8/3/16.
 */

public class CustomMarkerView extends MarkerView {

    private String imageUrl;
    private int reportId;

    public CustomMarkerView(BaseMarkerViewOptions baseMarkerViewOptions, String imageRef, int reportId) {
        super(baseMarkerViewOptions);
        this.imageUrl = imageRef;
        this.reportId = reportId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getReportId() {
        return reportId;
    }

}
