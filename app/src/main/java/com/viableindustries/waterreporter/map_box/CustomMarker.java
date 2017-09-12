package com.viableindustries.waterreporter.map_box;

import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
import com.mapbox.mapboxsdk.annotations.Marker;

/**
 * Created by brendanmcintyre on 8/3/16.
 */

public class CustomMarker extends Marker {

    private final String imageUrl;

    public CustomMarker(BaseMarkerOptions baseMarkerOptions, String imageRef) {
        super(baseMarkerOptions);
        this.imageUrl = imageRef;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
