package com.viableindustries.waterreporter.data;

/**
 * Created by brendanmcintyre on 6/12/17.
 */

public class CursorPositionTracker {

    private static int position;

    private static int hashIndex = 9999;

    public static int getPosition() {

        return position;

    }

    public static void setPosition(int aPosition) {

        CursorPositionTracker.position = aPosition;

    }

    public static int getHashIndex() {

        return hashIndex;

    }

    public static void setHashIndex(int aHashIndex) {

        CursorPositionTracker.hashIndex = aHashIndex;

    }

    public static void resetHashIndex() {

        CursorPositionTracker.hashIndex = 9999;

    }


}
