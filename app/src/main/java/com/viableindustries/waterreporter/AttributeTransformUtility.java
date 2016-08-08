package com.viableindustries.waterreporter;

import android.support.annotation.Nullable;

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

    public static String countComments(List<Comment> commentList) {

        if (commentList.size() != 1) {

            return String.format("%s comments", commentList.size());

        } else {

            return "1 comment";

        }

    }

    public static String parseWatershedName(Territory territory) {

        try {

            return String.format("%s Watershed", territory.properties.huc_6_name);

        } catch (NullPointerException ne) {

            return "Watershed not available";

        }

    }

}
