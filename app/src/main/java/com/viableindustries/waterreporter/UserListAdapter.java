package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserOrgPatch;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class UserListAdapter extends ArrayAdapter<User> implements Filterable {

    private final Context context;

    protected String name;

    protected int id;

    private ArrayList<User> sourceList;

    private ArrayList<User> filteredList;

//    private UserFilter mFilter;

    public UserListAdapter(Context context, ArrayList<User> features, boolean aShowLeaveButton) {

        super(context, 0, features);

        this.sourceList = features;

        this.filteredList = features;

        this.context = context;

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

        Picasso.with(context).load(user.properties.picture).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(viewHolder.userAvatar);

        // Add click listeners to layout elements

        viewHolder.userItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UserHolder.setUser(user);

                context.startActivity(new Intent(context, UserProfileActivity.class));

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