package com.viableindustries.waterreporter.utilities;

import android.content.res.Resources;
import android.text.format.DateUtils;
import android.view.Window;
import android.view.WindowManager;

import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.territory.Territory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by brendanmcintyre on 8/8/16.
 */

public class AttributeTransformUtility {

    public static List<Map<String, Integer>> buildImageRelation(int imageId) {

        List<Map<String, Integer>> images = new ArrayList<>();

        final Map<String, Integer> imageObject = new HashMap<>();

        imageObject.put("id", imageId);

        images.add(imageObject);

        return images;

    }

    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void setStatusBarTranslucent(Window window) {

        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

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

    public static String parseWatershedName(Territory territory, boolean appendLabel) {

        try {

            return appendLabel ? String.format("%s Watershed", territory.properties.huc_8_name) : territory.properties.huc_8_name;

        } catch (NullPointerException ne) {

            return "Watershed not available";

        }

    }

    public static CharSequence relativeTime(SimpleDateFormat dateFormatter, String dateString) {

        long delta;

        long currentTime = new Date().getTime();

        try {

            //create SimpleDateFormat object with source string date format
//            SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);

            //parse the string into Date object

            long originTime = dateFormatter.parse(dateString).getTime();

            delta = currentTime - originTime;

            // The value of delta is greater than or equal to 1 day (86400000 milliseconds)

            if (DateUtils.YEAR_IN_MILLIS <= delta) {

                SimpleDateFormat baseDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);

                //parse the date into another format
                dateString = baseDateFormat.format(dateFormatter.parse(dateString));

            } else if (86400000 <= delta && delta < DateUtils.YEAR_IN_MILLIS) {

                SimpleDateFormat baseDateFormat = new SimpleDateFormat("MMM d", Locale.US);

                //parse the date into another format
                dateString = baseDateFormat.format(dateFormatter.parse(dateString));

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
