//package com.viableindustries.waterreporter;
//
//import android.content.Context;
//import android.content.Intent;
//import android.support.annotation.NonNull;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.Filter;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//
//import com.squareup.picasso.Picasso;
//import com.viableindustries.waterreporter.data.Organization;
//import com.viableindustries.waterreporter.data.OrganizationHolder;
//
//import java.util.ArrayList;
//
///**
// * Created by brendanmcintyre on 2/10/17.
// */
//
//public class SearchResultListAdapter extends ArrayAdapter {
//
//    private final Context context;
//
//    protected String name;
//
//    protected int id;
//
//    private ArrayList sourceList;
//
//    public SearchResultListAdapter(Context aContext, ArrayList features, boolean aShowLeaveButton) {
//
//        super(aContext, 0, features);
//
//        this.sourceList = features;
//
//        this.mContext = aContext;
//
//    }
//
//    private static class ViewHolder {
//        ImageView imageView;
//        TextView resultName;
//        LinearLayout resultItem;
//    }
//
//    public int getCount() {
//
//        return sourceList.size();
//
//    }
//
////    public Organization getItem(int position) {
////
////        return filteredList.get(position);
////
////    }
//
//    @Override
//    @NonNull
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        SearchResultListAdapter.ViewHolder viewHolder;
//
//        if (convertView == null) {
//
//            convertView = LayoutInflater.from(getContext()).inflate(R.layout.search_result_item, parent, false);
//
//            viewHolder = new SearchResultListAdapter.ViewHolder();
//
//            viewHolder.organizationLogo = (ImageView) convertView.findViewById(R.id.organizationLogo);
//            viewHolder.organizationName = (TextView) convertView.findViewById(R.id.organizationName);
//            viewHolder.organizationItem = (LinearLayout) convertView.findViewById(R.id.organizationItem);
//
//            convertView.setTag(viewHolder);
//
//        } else {
//
//            viewHolder = (SearchResultListAdapter.ViewHolder) convertView.getTag();
//
//        }
//
//        final Organization organization = filteredList.get(position);
//
//        // Populate layout elements
//
//        viewHolder.organizationName.setText(organization.properties.name);
//
//        Picasso.with(context).load(organization.properties.picture).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(viewHolder.organizationLogo);
//
//        // Add click listeners to layout elements
//
//        viewHolder.organizationItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                OrganizationHolder.setOrganization(organization);
//
//                context.startActivity(new Intent(context, OrganizationProfileActivity.class));
//
//            }
//        });
//
//        return convertView;
//
//    }
//
//}
