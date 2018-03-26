package com.viableindustries.waterreporter.utilities;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.viableindustries.waterreporter.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Ryan Hamley on 10/28/14.
 * Some useful utility methods to use throughout the application
 */
public class UtilityMethods {

    private final Calendar c = Calendar.getInstance();

    public int getCurrentMonth() {
        return c.get(Calendar.MONTH);
    }

    public int getCurrentDay() {
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public int getCurrentYear() {
        return c.get(Calendar.YEAR);
    }

    public String getDateString(int aMonth, int aDay, int aYear) {
        String month = "";
        String day = String.valueOf(aDay);
        String year = String.valueOf(aYear);

        switch (aMonth) {
            case 0:
                month = "January";
                break;
            case 1:
                month = "February";
                break;
            case 2:
                month = "March";
                break;
            case 3:
                month = "April";
                break;
            case 4:
                month = "May";
                break;
            case 5:
                month = "June";
                break;
            case 6:
                month = "July";
                break;
            case 7:
                month = "August";
                break;
            case 8:
                month = "September";
                break;
            case 9:
                month = "October";
                break;
            case 10:
                month = "November";
                break;
            case 11:
                month = "December";
                break;
        }

        return month + " " + day + ", " + year;
    }

    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    public static String makeSecondaryListPostCountText(Resources resources, int postCount, String dateLastSeen) {

        String postCountText = String.format("%s %s", String.valueOf(postCount),
                resources.getQuantityString(R.plurals.post_label, postCount, postCount));

        String dateString = (String) AttributeTransformUtility.relativeTime(
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
                dateLastSeen);

        return String.format("%s · Last seen %s", postCountText, dateString);

    }

    public static long timeDelta(SimpleDateFormat dateFormatter, String dateString) {

        long delta = 0;

        long currentTime = new Date().getTime();

        try {

            // Parse the string into Date object

            long originTime = dateFormatter.parse(dateString).getTime();

            delta = currentTime - originTime;

            return delta;

        } catch (ParseException pe) {

            System.out.println("Parse Exception : " + pe);

        }

        return delta;

    }

}
