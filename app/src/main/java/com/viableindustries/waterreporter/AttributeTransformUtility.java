package com.viableindustries.waterreporter;

import android.support.annotation.Nullable;
import android.text.format.DateUtils;

import com.viableindustries.waterreporter.data.Comment;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.Territory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by brendanmcintyre on 8/8/16.
 */

public class AttributeTransformUtility {

    public static String getTerritoryCode(Territory territory) {

        String code = String.format("%s", territory.properties.huc_8_code);

        if (code.length() == 7) code = String.format("0%s", code);

        return code;

    }

    public static String parseDate(SimpleDateFormat simpleDateFormat, String dateString) {

        String parsedDate = "";

        try {
            //create SimpleDateFormat object with source string date format
            SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);

            //parse the string into Date object
            Date date = sdfSource.parse(dateString);

            //parse the date into another format
            parsedDate = simpleDateFormat.format(date);

        } catch (ParseException pe) {

            System.out.println("Parse Exception : " + pe);

        }

        return parsedDate;

    }

    public static String groupListSize(List<Organization> organizationList) {

        if (!organizationList.isEmpty()) {

            return organizationList.get(0).properties.name;

        } else {

            return "This report is not affiliated with any groups.";

        }

    }

    public static String parseWatershedName(Territory territory) {

        try {

            return String.format("%s Watershed", territory.properties.huc_8_name);

        } catch (NullPointerException ne) {

            return "Watershed not available";

        }

    }

    public static CharSequence relativeTime(String dateString) {

        long delta;

        long currentTime = new Date().getTime();

        try {

            //create SimpleDateFormat object with source string date format
            SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);

            //parse the string into Date object

            long originTime = sdfSource.parse(dateString).getTime();

            delta = currentTime - originTime;

            // The value of delta is greater than or equal to 1 day (86400000 milliseconds)

            if (delta >= 86400000) {

                SimpleDateFormat baseDateFormat = new SimpleDateFormat("MMM d", Locale.US);

                //parse the date into another format
                dateString = baseDateFormat.format(sdfSource.parse(dateString));

            } else if (3600000 <= delta && delta < 86400000) {

                int hours = Math.round(delta / 3600000);

                dateString = String.format("%sh", hours);

            } else {

                int minutes = Math.round(delta / 60000);

                dateString = minutes > 0 ? String.format("%sm", minutes) : String.format("%ss", Math.round(delta / 1000));

            }

        } catch (ParseException pe) {

            System.out.println("Parse Exception : " + pe);

        }

        return dateString;

    }

}
