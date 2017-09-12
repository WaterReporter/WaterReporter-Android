package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.data.objects.organization.Organization;
import com.viableindustries.waterreporter.utilities.CircleTransform;

import java.util.List;

public class RelatedGroupAdapter extends ArrayAdapter {

    private final Context mContext;

    public RelatedGroupAdapter(Context aContext, List features) {
        super(aContext, 0, features);
        this.mContext = aContext;
    }

    private static class ViewHolder {
        ImageView groupView;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.related_group_item, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.groupView = (ImageView) convertView.findViewById(R.id.groupLogo);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();

        }

        // Get the api item for this position
        Organization organization = (Organization) getItem(position);

        // Attach click listeners to active UI components

        // Populate the api into the template view using the api object

        try {

            Picasso.with(mContext).load(organization != null ? organization.properties.picture : null).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(viewHolder.groupView);

//            viewHolder.groupView.setText(organization.properties.name);

        } catch (NullPointerException ne) {

            viewHolder.groupView.setImageDrawable(null);

        }

        // Return the completed view to render on screen
        return convertView;

    }

}

