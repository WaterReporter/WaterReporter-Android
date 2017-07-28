package com.viableindustries.waterreporter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Organization;

import java.util.List;

public class RelatedGroupAdapter extends ArrayAdapter {

    private final Context context;

    public RelatedGroupAdapter(Context context, List features) {
        super(context, 0, features);
        this.context = context;
    }

    private static class ViewHolder {
        ImageView groupView;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.related_group_item, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.groupView = (ImageView) convertView.findViewById(R.id.groupLogo);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();

        }

        // Get the data item for this position
        Organization organization = (Organization) getItem(position);

        // Attach click listeners to active UI components

        // Populate the data into the template view using the data object

        try {

            Picasso.with(context).load(organization.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(viewHolder.groupView);

//            viewHolder.groupView.setText(organization.properties.name);

        } catch (NullPointerException ne) {

            viewHolder.groupView.setImageDrawable(null);

        }

        // Return the completed view to render on screen
        return convertView;

    }

}

