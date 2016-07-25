package com.viableindustries.waterreporter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TimelineAdapter extends ArrayAdapter {

    //public Context applicationContext = MainActivity.getAppContext();
    private final Context context;

    protected String creationDate;

    protected Integer featureId;

    protected String imagePath;

    protected String watershedName;

    protected List<String> groups;

    protected String groupList;

    public TimelineAdapter(Context context, List features) {
        super(context, 0, features);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the data item for this position
        Report feature = (Report) getItem(position);

        Log.d("groups", feature.properties.groups.toString());

//        List<Image> images = feature.images;

//        Class cls = images.getClass();

//        Log.v("array_type", cls.toString());

        ReportPhoto image = (ReportPhoto) feature.properties.images.get(0);

        imagePath = (String) image.properties.square_retina;
        ;

        //try {

        creationDate = (String) feature.properties.created;

        featureId = (Integer) feature.id;

        // Extract watershed name, if any

        try {

            watershedName = String.format("%s Watershed", feature.properties.territory.properties.huc_6_name);

        } catch (NullPointerException ne) {

            watershedName = "Watershed not available";

        }

        // Extract group names, if any

        if (!feature.properties.groups.isEmpty()) {

            groupList = feature.properties.groups.get(0).properties.name;

//            try {
//
//                for (Organization organization : feature.properties.groups) {
//
//                    groups.add(organization.properties.name);
//
//                }
//
//                groupList = groups.toString();
//
//            } catch (NullPointerException ne) {
//
//                //
//
//            }

        } else {

            groupList = "This report is not affiliated with any groups.";

        }

        try {
            //create SimpleDateFormat object with source string date format
            SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

            //SimpleDateFormat ISO8601DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.GERMANY);

            //parse the string into Date object
            Date date = sdfSource.parse(creationDate);

            //create SimpleDateFormat object with desired date format
//            SimpleDateFormat sdfOutput = new SimpleDateFormat("MMM dd, yyyy\nhh:mm a");
            SimpleDateFormat sdfOutput = new SimpleDateFormat("MMM dd, yyyy");

            //parse the date into another format
            creationDate = sdfOutput.format(date);

            //System.out.println("Date is converted from dd/MM/yy format to MM-dd-yyyy hh:mm:ss");
            //System.out.println("Converted date is : " + strDate);

        } catch (ParseException pe) {
            System.out.println("Parse Exception : " + pe);
        }

        //} catch (JSONException e) {

        //e.printStackTrace();

        //}

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, parent, false);
        }
        // Lookup view for data population
        TextView reportDate = (TextView) convertView.findViewById(R.id.report_date);
        TextView reportOwner = (TextView) convertView.findViewById(R.id.report_owner);
        TextView reportWatershed = (TextView) convertView.findViewById(R.id.report_watershed);
        TextView reportCaption = (TextView) convertView.findViewById(R.id.report_caption);
        TextView reportGroups = (TextView) convertView.findViewById(R.id.report_groups);
        ImageView ownerAvatar = (ImageView) convertView.findViewById(R.id.owner_avatar);
        ImageView reportThumb = (ImageView) convertView.findViewById(R.id.report_thumb);
        LinearLayout reportStub = (LinearLayout) convertView.findViewById(R.id.report_stub);

        // Populate the data into the template view using the data object
        reportDate.setText(creationDate);
        reportOwner.setText(String.format("%s %s", feature.properties.owner.properties.first_name, feature.properties.owner.properties.last_name));
        reportWatershed.setText(watershedName);
        reportCaption.setText(feature.properties.report_description.trim());
        reportGroups.setText(groupList);

        reportStub.setTag(featureId);

        Log.v("url", imagePath);

        Picasso.with(context).load(feature.properties.owner.properties.picture).transform(new CircleTransform()).into(ownerAvatar);

        Picasso.with(context).load(imagePath).fit().centerCrop().into(reportThumb);

        // Return the completed view to render on screen
        return convertView;

    }

}

