package com.viableindustries.waterreporter;

import java.util.Calendar;

/**
 * Created by Ryan Hamley on 10/28/14.
 * Some useful utility methods to use throughout the application
 */
class UtilityMethods {

    private final Calendar c = Calendar.getInstance();

    public int getCurrentMonth(){
        return c.get(Calendar.MONTH);
    }

    public int getCurrentDay(){
        return c.get(Calendar.DAY_OF_MONTH);
    }

    public int getCurrentYear(){
        return c.get(Calendar.YEAR);
    }

    public String getDateString(int aMonth, int aDay, int aYear){
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
}
