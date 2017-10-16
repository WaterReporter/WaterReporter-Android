package com.viableindustries.waterreporter.user_interface.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.api.models.hashtag.HashTag;
import com.viableindustries.waterreporter.api.models.hashtag.TagHolder;
import com.viableindustries.waterreporter.utilities.CursorPositionTracker;

import java.util.List;

/**
 * Created by brendanmcintyre on 4/24/17.
 */

public class TagSuggestionAdapter extends ArrayAdapter<HashTag> {

    private final Context mContext;

    protected String name;

    protected int id;

    private final List<HashTag> sourceList;

    public TagSuggestionAdapter(Context aContext, List<HashTag> features) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

    }

    private static class ViewHolder {
        TextView tagName;
        TextView useCount;
        LinearLayout tagItem;
    }

    public int getCount() {

        return sourceList.size();

    }

    public HashTag getItem(int position) {

        return sourceList.get(position);

    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final TagSuggestionAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tag_suggestion_item, parent, false);

            viewHolder = new TagSuggestionAdapter.ViewHolder();

            viewHolder.tagName = (TextView) convertView.findViewById(R.id.tag_name);
            viewHolder.useCount = (TextView) convertView.findViewById(R.id.use_count);
            viewHolder.tagItem = (LinearLayout) convertView.findViewById(R.id.tag_item);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (TagSuggestionAdapter.ViewHolder) convertView.getTag();

        }

        final HashTag hashTag = sourceList.get(position);

        // Populate layout elements

        viewHolder.tagName.setText(String.format("\u0023%s", hashTag.properties.tag));

        int reportCount = hashTag.properties.reports.size();

        if (reportCount > 0) {

            viewHolder.useCount.setVisibility(View.VISIBLE);

            String quantityString = mContext.getResources().getQuantityString(R.plurals.post_label_lower, reportCount, reportCount);

            viewHolder.useCount.setText(String.format("%s %s", hashTag.properties.reports.size(), quantityString));

        } else {

            viewHolder.useCount.setVisibility(View.GONE);

        }

        // Add click listeners to layout elements

        viewHolder.tagItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Retrieve boundary indices

                int cursorPosition = CursorPositionTracker.getPosition();

                int hashIndex = CursorPositionTracker.getHashIndex();

                EditText postCaptionView = (EditText) ((Activity) mContext).findViewById(R.id.comment_input);

                HorizontalScrollView tagList = (HorizontalScrollView) ((Activity) mContext).findViewById(R.id.tag_component);

                String postCaptionText = postCaptionView.getText().toString();

                // Assemble substrings

                String partialA = postCaptionText.substring(0, hashIndex);

                String partialB = postCaptionText.substring(cursorPosition, postCaptionText.length());

                String tag = String.format("\u0023%s ", hashTag.properties.tag);

                String complete = String.format("%s%s%s", partialA, tag, partialB);

                // Update input text

                postCaptionView.setText(complete);

                // Update cursor position so that user remains in the correct place

                postCaptionView.setSelection(hashIndex + tag.length());

                // Reset octothorpe index to default value

                CursorPositionTracker.resetHashIndex();

                TagHolder.setCurrent(hashTag.properties.tag);

                tagList.setVisibility(View.GONE);

            }
        });

        return convertView;

    }

}