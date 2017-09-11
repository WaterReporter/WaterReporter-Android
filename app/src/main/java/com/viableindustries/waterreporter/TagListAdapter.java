package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viableindustries.waterreporter.data.HashTag;

import java.util.List;

/**
 * Created by brendanmcintyre on 4/21/17.
 */

public class TagListAdapter extends ArrayAdapter<HashTag> {

    private final Context mContext;

    protected String name;

    protected int id;

    private List<HashTag> sourceList;

    public TagListAdapter(Context aContext, List<HashTag> features, boolean aShowLeaveButton) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

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
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final TagListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.tag_list_item, parent, false);

            viewHolder = new TagListAdapter.ViewHolder();

            viewHolder.tagName = (TextView) convertView.findViewById(R.id.tag_name);
            viewHolder.tagItem = (LinearLayout) convertView.findViewById(R.id.tag_item);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (TagListAdapter.ViewHolder) convertView.getTag();

        }

        final HashTag hashTag = sourceList.get(position);

        // Populate layout elements

        viewHolder.tagName.setText(String.format("\u0023%s", hashTag.properties.tag));

        // Add click listeners to layout elements

        viewHolder.tagItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(mContext, TagProfileActivity.class);

                intent.putExtra("tag", String.format("\u0023%s", hashTag.properties.tag));

                mContext.startActivity(intent);

            }
        });

        return convertView;

    }

}
