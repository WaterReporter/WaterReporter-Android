package com.viableindustries.waterreporter;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

/**
 * Created by brendanmcintyre on 4/12/17.
 */

public class PostTextProcessor {

    public static SpannableStringBuilder process(String text) {

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

        String[] tokens = text.trim().split("\\s+");

        for (String token : tokens) {

            if (token.contains("#")) {

                int start = stringBuilder.length();

                stringBuilder.append(String.format("%s ", token));

                stringBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, stringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            } else {

                stringBuilder.append(String.format("%s ", token));

            }

        }

        return stringBuilder;

    }

}
