package com.viableindustries.waterreporter;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by brendanmcintyre on 8/30/17.
 */

class PostHeaderGestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final String DEBUG_TAG = "Gestures";

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
        return true;
    }

}
