package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserHolder;

import java.util.List;


public class UserListAdapter extends ArrayAdapter<User> implements Filterable {

    private final Context mContext;

    protected String name;

    protected int id;

    private List<User> sourceList;

    private List<User> filteredList;

//    private UserFilter mFilter;

    public UserListAdapter(Context aContext, List<User> features, boolean aShowLeaveButton) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.filteredList = features;

        this.mContext = aContext;

    }

    private static class ViewHolder {
        ImageView userAvatar;
        TextView userName;
        LinearLayout userItem;
    }

    public int getCount() {

        return filteredList.size();

    }

    public User getItem(int position) {

        return filteredList.get(position);

    }

//    @NonNull
//    public Filter getFilter() {
//
//        // TODO Auto-generated method stub
//
//        if (mFilter == null) {
//
//            mFilter = new UserFilter();
//
//        }
//
//        return mFilter;
//
//    }

    @Override
    @NonNull
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_list_item, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.userAvatar = (ImageView) convertView.findViewById(R.id.userAvatar);
            viewHolder.userName = (TextView) convertView.findViewById(R.id.userName);
            viewHolder.userItem = (LinearLayout) convertView.findViewById(R.id.userItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();

        }

        final User user = filteredList.get(position);

        // Populate layout elements

        viewHolder.userName.setText(String.format("%s %s", user.properties.first_name, user.properties.last_name));

        Picasso.with(mContext).load(user.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(viewHolder.userAvatar);

        // Add click listeners to layout elements

        viewHolder.userItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UserHolder.setUser(user);

                mContext.startActivity(new Intent(mContext, UserProfileActivity.class));

            }
        });

        return convertView;

    }

//    private class UserFilter extends Filter {
//
//        @Override
//        protected FilterResults performFiltering(CharSequence constraint) {
//
//            FilterResults results = new FilterResults();
//
//            // Perform filtering operation
//            // May need to implement partial/fuzzy matching as the number of users grows
//
//            ArrayList<User> nOrgList = new ArrayList<>();
//
//            for (User org : sourceList) {
//
//                if (org.properties.name.toUpperCase().startsWith(constraint.toString().toUpperCase())) {
//
//                    Log.d("name", org.properties.name);
//
//                    nOrgList.add(org);
//
//                }
//
//            }
//
//            results.values = nOrgList;
//
//            results.count = nOrgList.size();
//
//            return results;
//
//        }
//
//        // Probably not the best idea, need to find a better solution
//        @SuppressWarnings("unchecked")
//        @Override
//        protected void publishResults(CharSequence constraint,
//                                      FilterResults results) {
//
//            // Inform the adapter about the new filtered list
//
//            if (results.count == 0) {
//
//                notifyDataSetInvalidated();
//
//            } else {
//
//                filteredList = (ArrayList<User>) results.values;
//
//                notifyDataSetChanged();
//
//            }
//
//        }
//
//    }

}