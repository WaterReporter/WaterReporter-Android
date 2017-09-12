package com.viableindustries.waterreporter.utilities;

import java.text.DecimalFormat;

/**
 * Created by brendanmcintyre on 11/14/16.
 */

public class DisplayDecimal {

    public static String formatDecimals(String template, String output, Double... doubles) {

        DecimalFormat df = new DecimalFormat(template);

        String[] formatted = new String[doubles.length];

        int index = 0;

        for (Double d : doubles) {

            formatted[index++] = df.format(d);

        }

        return String.format(output, formatted);

    }

}
