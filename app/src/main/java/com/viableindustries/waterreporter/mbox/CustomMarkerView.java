package com.viableindustries.waterreporter.mbox;

import com.mapbox.mapboxsdk.annotations.BaseMarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.MarkerView;

/**
 * Created by brendanmcintyre on 8/3/16.
 */

public class CustomMarkerView extends MarkerView {

    private String imageUrl;

    public CustomMarkerView(BaseMarkerViewOptions baseMarkerViewOptions, String imageRef) {
        super(baseMarkerViewOptions);
        this.imageUrl = imageRef;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
