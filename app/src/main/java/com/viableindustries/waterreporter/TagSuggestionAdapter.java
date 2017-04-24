package com.viableindustries.waterreporter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.viableindustries.waterreporter.data.HashTag;
import com.viableindustries.waterreporter.data.TagHolder;

import java.util.List;

/**
 * Created by brendanmcintyre on 4/24/17.
 */

public class TagSuggestionAdapter extends ArrayAdapter<HashTag> {

    private final Context context;

    protected String name;

    protected int id;

    private List<HashTag> sourceList;

    public TagSuggestionAdapter(Context context, List<HashTag> features) {

        super(context, 0, features);

        this.sourceList = features;

        this.context = context;

    }

    private static class ViewHolder {
        TextView tagName;
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
    public View getView(int position, View convertView, ViewGroup parent) {

        final TagSuggestionAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tag_list_item, parent, false);

            viewHolder = new TagSuggestionAdapter.ViewHolder();

            viewHolder.tagName = (TextView) convertView.findViewById(R.id.tag_name);
            viewHolder.tagItem = (LinearLayout) convertView.findViewById(R.id.tag_item);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (TagSuggestionAdapter.ViewHolder) convertView.getTag();

        }

        final HashTag hashTag = sourceList.get(position);

        // Populate layout elements

        viewHolder.tagName.setText(String.format("\u0023%s", hashTag.properties.tag));

        // Add click listeners to layout elements

        viewHolder.tagItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TextView postCaptionView = (TextView) ((Activity) context).findViewById(R.id.comment_input);

                HorizontalScrollView tagList = (HorizontalScrollView) ((Activity) context).findViewById(R.id.tag_component);

                String postCaptionText = postCaptionView.getText().toString();

                postCaptionText = postCaptionText.substring(0, postCaptionText.lastIndexOf("#"));

                postCaptionText += String.format("\u0023%s", hashTag.properties.tag);

                postCaptionView.setText(postCaptionText);

                TagHolder.setCurrent(hashTag.properties.tag);

                tagList.setVisibility(View.GONE);

            }
        });

        return convertView;

    }

}
