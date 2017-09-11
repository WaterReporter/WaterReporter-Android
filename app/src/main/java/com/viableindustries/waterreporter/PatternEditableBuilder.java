package com.viableindustries.waterreporter;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
     Create clickable spans within a TextView
     made easy with pattern matching!

     Created by: Nathan Esquenazi

     Usage 1: Apply spannable strings to a TextView based on pattern

        new PatternEditableBuilder().
           addPattern(Pattern.compile("\\@(\\w+)")).
           into(textView);

     Usage 2: Apply clickable spans to a TextView

         new PatternEditableBuilder().
             addPattern(Pattern.compile("\\@(\\w+)"), Color.BLUE,
             new PatternEditableBuilder.SpannableClickedListener() {
                @Override
                public void onSpanClicked(String text) {
                    // Do something here
                }
             }).into(textView);

     See README for more details.
 */

public class PatternEditableBuilder {
    // Records the pattern spans to apply to a TextView
    private final ArrayList<SpannablePatternItem> patterns;

    /* This stores a particular pattern item
       complete with pattern, span styles, and click listener */
    public class SpannablePatternItem {

        public final Context mContext;
        public final Pattern pattern;
        public final int textColor;
        public final SpannableClickedListener listener;

        public SpannablePatternItem(Context aContext, Pattern pattern, int textColor, SpannableClickedListener listener) {
            this.mContext = aContext;
            this.pattern = pattern;
            this.textColor = textColor;
            this.listener = listener;
        }
    }

    /* This stores the style listener for a pattern item
       Used to style a particular category of spans */
    public static abstract class SpannableStyleListener {

        public int spanTextColor;

        public SpannableStyleListener() {
        }

        public SpannableStyleListener(int spanTextColor) {
            this.spanTextColor = spanTextColor;
        }

        abstract void onSpanStyled(TextPaint ds);
    }

    /* This stores the click listener for a pattern item
       Used to handle clicks to a particular category of spans */
    public interface SpannableClickedListener {
        void onSpanClicked(String text);
    }

    /* This is the custom clickable span class used
       to handle user clicks to our pattern spans
       applying the styles and invoking click listener.
     */
    public class StyledClickableSpan extends ClickableSpan {

        final SpannablePatternItem item;

        public StyledClickableSpan(SpannablePatternItem item) {
            this.item = item;
        }

        @Override
        public void updateDrawState(TextPaint ds) {

            super.updateDrawState(ds);

            ds.setColor(item.textColor);
            ds.bgColor = Color.WHITE;
            ds.setUnderlineText(false);

        }

        @Override
        public void onClick(View widget) {
            if (item.listener != null) {
                TextView tv = (TextView) widget;
                Spanned span = (Spanned) tv.getText();
                int start = span.getSpanStart(this);
                int end = span.getSpanEnd(this);
                CharSequence text = span.subSequence(start, end);
                item.listener.onSpanClicked(text.toString());

            }
            widget.invalidate();
        }
    }

    /* ----- Constructors ------- */
    public PatternEditableBuilder() {
        this.patterns = new ArrayList<>();
    }

    /* These are the `addPattern` overloaded signatures */
    // Each allows us to add a span pattern with different arguments
    public PatternEditableBuilder addPattern(Context aContext, Pattern pattern, int textColor, SpannableClickedListener listener) {
        patterns.add(new SpannablePatternItem(aContext, pattern, textColor, listener));
        return this;
    }

//    public PatternEditableBuilder addPattern(Pattern pattern, SpannableStyleListener spanStyles) {
//        addPattern(pattern, spanStyles, null);
//        return this;
//    }

//    public PatternEditableBuilder addPattern(Pattern pattern) {
//        addPattern(pattern, null, null);
//        return this;
//    }

//    public PatternEditableBuilder addPattern(Pattern pattern, int textColor) {
//        addPattern(pattern, textColor, null);
//        return this;
//    }

//    public PatternEditableBuilder addPattern(Pattern pattern, int textColor, SpannableClickedListener listener) {
////        SpannableStyleListener styles = new SpannableStyleListener(textColor) {
////            @Override
////            public void onSpanStyled(TextPaint ds) {
////                ds.linkColor = this.spanTextColor;
////            }
////        };
//        addPattern(pattern, textColor, listener);
//        return this;
//    }

//    public PatternEditableBuilder addPattern(Pattern pattern, SpannableClickedListener listener) {
//        addPattern(pattern, null, listener);
//        return this;
//    }

    /* BUILDER METHODS */

    // This builds the pattern span and applies to a TextView
    public void into(TextView textView) {
        SpannableStringBuilder result = build(textView.getText());
        textView.setText(result);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    // This builds the pattern span into a `SpannableStringBuilder`
    // Requires a CharSequence to be passed in to be applied to
    private SpannableStringBuilder build(CharSequence editable) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(editable);
        for (SpannablePatternItem item : patterns) {
            Matcher matcher = item.pattern.matcher(ssb);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                StyledClickableSpan url = new StyledClickableSpan(item);
                ssb.setSpan(url, start, end, 0);
            }
        }
        return ssb;
    }

}