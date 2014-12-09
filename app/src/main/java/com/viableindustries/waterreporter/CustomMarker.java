package com.viableindustries.waterreporter;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * Created by Ryan Hamley on 10/14/14.
 * This custom marker class returns an InfoWindow using our custom layout.
 */
public class CustomMarker extends Marker {

    public CustomMarker(MapView mv, String aTitle, String aDescription, LatLng aLatLng){
        super(mv, aTitle, aDescription, aLatLng);
    }

    @Override
    protected InfoWindow createTooltip(MapView mv) {
        return new InfoWindow(R.layout.custom_tooltip, mv);
    }
}
