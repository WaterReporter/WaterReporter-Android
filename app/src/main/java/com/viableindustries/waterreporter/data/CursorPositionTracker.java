package com.viableindustries.waterreporter.data;

/**
 * Created by brendanmcintyre on 6/12/17.
 */

public class CursorPositionTracker {

    private static int start;

    private static int end;

    private static int signIndex = 9999;

    public static int getStart() {

        return start;

    }

    public static void setStart(int aStart) {

        CursorPositionTracker.start = aStart;

    }

    public static int getEnd() {

        return end;

    }

    public static void setEnd(int aEnd) {

        CursorPositionTracker.end = aEnd;

    }

    public static int getSignIndex() {

        return signIndex;

    }

    public static void setSignIndex(int aSignIndex) {

        CursorPositionTracker.signIndex = aSignIndex;

    }

}
