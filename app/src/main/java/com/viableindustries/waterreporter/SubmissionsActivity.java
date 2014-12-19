package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orm.query.Select;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.CommonsCloudService;
import com.viableindustries.waterreporter.data.Geometries;
import com.viableindustries.waterreporter.data.GeometryResponse;
import com.viableindustries.waterreporter.data.PostResponse;
import com.viableindustries.waterreporter.data.Submission;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

/**
 * Created by Ryan Hamley on 10/28/14.
 * Activity displays a list of user submissions which can be submitted to the API via pull
 * to refresh action
 */
public class SubmissionsActivity extends ActionBarActivity
        implements SwipeRefreshLayout.OnRefreshListener {
    @InjectView(R.id.swipe_container) SwipeRefreshLayout swipeRefreshLayout;
    @InjectView(R.id.listview_submissions) ListView submissionsListView;

    private List<Submission> submissions;
    private SubmittedAdapter adapter;
    private boolean postFailed = false;
    private static final String onResume = "onResume";
    private static final String onRefresh = "onRefresh";
    private static final String NAME_KEY = "user_name";
    private static final String EMAIL_KEY = "user_email";
    private static final String TITLE_KEY = "user_title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submissions);

        ButterKnife.inject(this);

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.waterreporter_blue,
                R.color.waterreporter_dark);

        createListElements();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!swipeRefreshLayout.isRefreshing()){
            submitNewReports(onResume);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        submissions.clear();
        createListElements();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.submissions, menu);
        return true;
    }

    @Override
    public void onRefresh() {
        submitNewReports(onRefresh);
    }

    protected void createListElements(){
        submissions = Select.from(Submission.class).orderBy("id DESC").list();

        adapter = new SubmittedAdapter();
        submissionsListView.setAdapter(adapter);

        submissionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Submission submission = submissions.get(i);
                Long id = submission.getId();
                Intent intent = new Intent(getBaseContext(), SubmissionDetailActivity.class)
                        .putExtra("SubmissionId", id);
                startActivity(intent);
            }
        });
    }

    protected void onPostSuccess(Submission submission, PostResponse postResponse){
        submission.feature_id = postResponse.resource_id;
        submission.save();
        adapter.notifyDataSetChanged();

        if(postFailed){
            postFailed = false;
        }

        submissionsListView.invalidateViews();
        swipeRefreshLayout.setRefreshing(false);
    }

    protected void onPostError(){
        swipeRefreshLayout.setRefreshing(false);
        postFailed = true;
        submissionsListView.invalidateViews();
        CharSequence text =
                "Error posting reports. Try again later.";
        Toast toast = Toast.makeText(getBaseContext(), text,
                Toast.LENGTH_SHORT);
        toast.show();
    }

    protected void submitNewReports(String method){
        RestAdapter restAdapter = CommonsCloudService.restAdapter;

        CommonsCloudService service = restAdapter.create(CommonsCloudService.class);

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);
        String name = prefs.getString(NAME_KEY, "");
        String email = prefs.getString(EMAIL_KEY, "");
        String title = prefs.getString(TITLE_KEY, "");

        String pollutionType, activityType;

        ArrayList<Float> coordinates = new ArrayList<Float>(2);
        String point = "Point";
        String type = "GeometryCollection";
        List<Geometries> geometriesList = new ArrayList<Geometries>(1);
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        int count = 0;

        for(final Submission submission : submissions){
            if(submission.feature_id == 0){
                coordinates.clear();
                geometriesList.clear();

                coordinates.add((float) submission.longitude);
                coordinates.add((float) submission.latitude);
                Geometries geometries = new Geometries(coordinates, point);
                geometriesList.add(geometries);
                GeometryResponse geometryResponse = new GeometryResponse(geometriesList, type);

                boolean isActivity;

                if(submission.type.equals("[{\"id\":1}]")){
                    activityType = submission.activity;
                    pollutionType = null;
                    isActivity = true;
                } else {
                    pollutionType = submission.activity;
                    activityType = null;
                    isActivity = false;
                }

                if(isActivity){
                    if(submission.photoPath != null){
                        File photo = new File(submission.photoPath);
                        String mimeType = fileNameMap.getContentTypeFor(submission.photoPath);
                        TypedFile typedPhoto = new TypedFile(mimeType, photo);

                        service.postActivityReportWithPhoto(submission.comments, email, name, title,
                                submission.date, submission.type, activityType, "public",
                                new Gson().toJson(geometryResponse), typedPhoto,
                                new Callback<PostResponse>() {
                                    @Override
                                    public void success(PostResponse postResponse,
                                                        Response response) {
                                        onPostSuccess(submission, postResponse);
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        onPostError();
                                    }
                                });
                    } else {
                        service.postActivityReport(submission.comments, email, name, title,
                                submission.date, submission.type, activityType, "public",
                                new Gson().toJson(geometryResponse), new Callback<PostResponse>() {
                                    @Override
                                    public void success(PostResponse postResponse,
                                                        Response response) {
                                        onPostSuccess(submission, postResponse);
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        onPostError();
                                    }
                                });
                    }
                } else {
                    if(submission.photoPath != null){
                        File photo = new File(submission.photoPath);
                        String mimeType = fileNameMap.getContentTypeFor(submission.photoPath);
                        TypedFile typedPhoto = new TypedFile(mimeType, photo);

                        service.postPollutionReportWithPhoto(submission.comments, email, name, title,
                                submission.date, submission.type, pollutionType, "public",
                                new Gson().toJson(geometryResponse), typedPhoto,
                                new Callback<PostResponse>() {
                                    @Override
                                    public void success(PostResponse postResponse,
                                                        Response response) {
                                        onPostSuccess(submission, postResponse);
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        onPostError();
                                    }
                                });
                    } else {
                        service.postPollutionReport(submission.comments, email, name, title,
                                submission.date, submission.type, pollutionType, "public",
                                new Gson().toJson(geometryResponse), new Callback<PostResponse>() {
                                    @Override
                                    public void success(PostResponse postResponse,
                                                        Response response) {
                                        onPostSuccess(submission, postResponse);
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        onPostError();
                                    }
                                });
                    }
                }
            } else {
                count++;
            }

            if(count == submissions.size() && method.equals(onRefresh)){
                swipeRefreshLayout.setRefreshing(false);
                CharSequence text = "All reports submitted!";
                Toast toast = Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private static class SubmittedViewHolder {
        public ImageView checkMark;
        public TextView text;
    }

    private class SubmittedAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return submissions.size();
        }

        @Override
        public Submission getItem(int position) {
            return submissions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SubmittedViewHolder holder;

            String type;
            int id = Integer.parseInt(getItem(position).type.replaceAll("\\D+",""));

            if(id == 1){
                type = "Activity Report";
            } else {
                type = "Pollution Report";
            }

            if(convertView == null){
                convertView = getLayoutInflater()
                        .inflate(R.layout.list_item_submission, parent, false);
                holder = new SubmittedViewHolder();
                holder.text = (TextView)
                        convertView.findViewById(R.id.list_item_submission_textview);
                holder.text.setText(type + " on " + getItem(position).date);
                convertView.setTag(holder);
            } else {
                holder = (SubmittedViewHolder) convertView.getTag();
            }

            if(getItem(position).feature_id != 0) {
                holder.checkMark = (ImageView) convertView.findViewById(R.id.ok_image);
                Picasso.with(getBaseContext()).load(R.drawable.ic_done_grey600_24dp)
                        .into(holder.checkMark);
            } else if(getItem(position).feature_id == 0 && !postFailed){
                holder.checkMark = (ImageView) convertView.findViewById(R.id.ok_image);
                Picasso.with(getBaseContext()).load(R.drawable.upload).resize(75, 75)
                        .into(holder.checkMark);
            } else if(getItem(position).feature_id == 0 && postFailed){
                holder.checkMark = (ImageView) convertView.findViewById(R.id.ok_image);
                Picasso.with(getBaseContext()).load(R.drawable.warning).resize(75, 75)
                        .into(holder.checkMark);
            }

            return convertView;
        }
    }
}