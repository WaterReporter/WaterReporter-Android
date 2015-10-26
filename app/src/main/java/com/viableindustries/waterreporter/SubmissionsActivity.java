package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orm.query.Select;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.ImageProperties;
import com.viableindustries.waterreporter.data.ImageService;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.viableindustries.waterreporter.data.ReportPostBody;
import com.viableindustries.waterreporter.data.ReportPostResponse;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.GeometryResponse;
import com.viableindustries.waterreporter.data.Submission;
import com.viableindustries.waterreporter.progress.CountingTypedFile;
import com.viableindustries.waterreporter.progress.ProgressListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
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
public class SubmissionsActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    @Bind(R.id.swipe_container)
    SwipeRefreshLayout swipeRefreshLayout;

    @Bind(R.id.listview_submissions)
    ListView submissionsListView;

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

//        Toolbar toolbar = (Toolbar) findViewById(R.id.wr_toolbar);
//
//        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        swipeRefreshLayout.setOnRefreshListener(this);

        swipeRefreshLayout.setColorSchemeResources(R.color.waterreporter_blue,
                R.color.waterreporter_dark);

        createListElements();
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (!swipeRefreshLayout.isRefreshing()) {

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

    protected void createListElements() {

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

    protected void onPostSuccess(Submission submission, Report report) {

        submission.feature_id = report.id;

        submission.save();

        adapter.notifyDataSetChanged();

        if (postFailed) {

            postFailed = false;

        }

        submissionsListView.invalidateViews();

        swipeRefreshLayout.setRefreshing(false);

    }

    protected void onPostError() {

        swipeRefreshLayout.setRefreshing(false);

        postFailed = true;

        submissionsListView.invalidateViews();

        CharSequence text =
                "Error posting reports. Try again later.";

        Toast toast = Toast.makeText(getBaseContext(), text,
                Toast.LENGTH_SHORT);
        toast.show();

    }

    protected void submitNewReports(String method) {

        final ReportService reportService = ReportService.restAdapter.create(ReportService.class);

        final ImageService imageService = ImageService.restAdapter.create(ImageService.class);

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String access_token = prefs.getString("access_token", "");

        ArrayList<Float> coordinates = new ArrayList<Float>(2);

        String point = "Point";

        String type = "GeometryCollection";

        List<Geometry> geometryList = new ArrayList<Geometry>(1);

        FileNameMap fileNameMap = URLConnection.getFileNameMap();

        int count = 0;

        for (final Submission submission : submissions) {

            //final long submissionId = submission.getId();

            if (submission.feature_id == 0) {

                coordinates.clear();
                geometryList.clear();

                coordinates.add((float) submission.longitude);
                coordinates.add((float) submission.latitude);
                Geometry geometry = new Geometry(coordinates, point);
                geometryList.add(geometry);

                final GeometryResponse geometryResponse = new GeometryResponse(geometryList, type);

                if (submission.photoPath != null) {

                    //savePhoto(submission.photoPath);
                    Log.d("filepath", submission.photoPath);

                    File photo = new File(submission.photoPath);

                    String mimeType = fileNameMap.getContentTypeFor(submission.photoPath);

                    TypedFile typedPhoto = new TypedFile(mimeType, photo);

                    imageService.postImage(access_token, typedPhoto,
                            new Callback<ImageProperties>() {
                                @Override
                                public void success(ImageProperties imageProperties,
                                                    Response response) {

                                    // Update the on-device record with the new remote image location

                                    Log.d("properties-gallery-path", imageProperties.square_retina);

                                    //Submission submission = Submission.findById(Submission.class, submissionId);

                                    submission.galleryPath = imageProperties.square_retina;

                                    submission.save();

                                    // Retrieve the image id and create a new report

                                    Map<String, Integer> image_id = new HashMap<String, Integer>();

                                    image_id.put("id", imageProperties.id);

                                    List<Map<String, Integer>> images = new ArrayList<Map<String, Integer>>();

                                    images.add(image_id);

                                    ReportPostBody reportPostBody = new ReportPostBody(geometryResponse,
                                            images, true, submission.report_date, submission.report_description, "public");

                                    reportService.postReport(access_token, "application/json", reportPostBody,
                                            new Callback<Report>() {
                                                @Override
                                                public void success(Report report,
                                                                    Response response) {
                                                    onPostSuccess(submission, report);
                                                }

                                                @Override
                                                public void failure(RetrofitError error) {
                                                    onPostError();
                                                }
                                            });

                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    onPostError();
                                }
                            });

                }

            } else {
                count++;
            }

            if (count == submissions.size() && method.equals(onRefresh)) {
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
        public ProgressBar progressBar;
        public View saved;
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

            if (convertView == null) {
                convertView = getLayoutInflater()
                        .inflate(R.layout.list_item_submission, parent, false);
                holder = new SubmittedViewHolder();
                holder.text = (TextView)
                        convertView.findViewById(R.id.list_item_submission_textview);
                holder.text.setText("Submitted on " + getItem(position).report_date);
                convertView.setTag(holder);
            } else {
                holder = (SubmittedViewHolder) convertView.getTag();
            }

            holder.checkMark = (ImageView) convertView.findViewById(R.id.ok_image);

            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.report_spinner);

            holder.progressBar.getIndeterminateDrawable().setColorFilter(
                    getResources().getColor(R.color.base_blue),
                    android.graphics.PorterDuff.Mode.SRC_IN);

            if (getItem(position).feature_id != 0) {
//                holder.checkMark = (ImageView) convertView.findViewById(R.id.ok_image);
                holder.progressBar.setVisibility(View.GONE);

                holder.saved = (View) convertView.findViewById(R.id.saved);

                holder.saved.setVisibility(View.VISIBLE);
//                Picasso.with(getBaseContext()).load(R.drawable.ic_done_grey600_24dp)
//                        .into(holder.checkMark);
            } else if (getItem(position).feature_id == 0 && !postFailed) {
                Log.d(null, "save in progress");
//                holder.checkMark = (ImageView) convertView.findViewById(R.id.ok_image);
//                Picasso.with(getBaseContext()).load(R.drawable.upload).resize(75, 75)
//                        .into(holder.checkMark);
//                holder.progressBar.setVisibility(View.VISIBLE);
            } else if (getItem(position).feature_id == 0 && postFailed) {
//                holder.checkMark = (ImageView) convertView.findViewById(R.id.ok_image);
                Picasso.with(getBaseContext()).load(R.drawable.warning).resize(75, 75)
                        .into(holder.checkMark);
            }

            return convertView;
        }
    }

//    private class SendFileTask extends AsyncTask<String, Integer, ReportPhoto> {
//
//        private ProgressListener listener;
//
//        private String filePath;
//
//        private String mimeType;
//
//        private SharedPreferences prefs =
//                getSharedPreferences(getPackageName(), MODE_PRIVATE);
//
//        private final String access_token = prefs.getString("access_token", "");
//
//        public SendFileTask(String filePath, String mimeType) {
//
//            this.filePath = filePath;
//
//            this.mimeType = mimeType;
//
//        }
//
//        @Override
//        protected ReportPhoto doInBackground(String... params) {
//
//            File file = new File(filePath);
//
//            final long totalSize = file.length();
//
//            Log.d("Upload FileSize[%d]", totalSize + "");
//
//            listener = new ProgressListener() {
//
//                @Override
//                public void transferred(long num) {
//
//                    publishProgress((int) ((num / (float) totalSize) * 100));
//
//                }
//
//            };
//
////            String _fileType = mimeType.equals(fileType) ? "video/mp4" : (FileType.IMAGE.equals(fileType) ? "image/jpeg" : "*/*");
//
//            CountingTypedFile typedPhoto = new CountingTypedFile(mimeType, file, listener);
//
//            return ImageService.restAdapter.create(ImageService.class).postImage(access_token, typedPhoto);
//
//        }
//
//        @Override
//        protected void onPostExecute(ReportPhoto reportPhoto) {
//
//            // Retrieve image id to associate with report
//            mImageId = reportPhoto.id;
//
//            Intent intent = new Intent(PhotoActivity.this, PhotoMetaActivity.class);
//
//            // Pass the on-device file path and API image id with the intent
//            intent
//                    .putExtra("image_id", mImageId)
//                    .putExtra("image_path", mCurrentPhotoPath);
//
//            startActivity(intent);
//
//        }
//
//        @Override
//        protected void onProgressUpdate(Integer... values) {
//            Log.d(null, String.format("progress[%d]", values[0]));
//            //do something with values[0], its the percentage so you can easily do
//
//            // Set progress
//            //mPhotoBar.setProgress(values[0]);
//
//        }
//
//    }

//    protected void savePhoto(String filePath) {
//
//        RestAdapter restAdapter = ReportService.restAdapter;
//
//        ImageService imageService = restAdapter.create(ImageService.class);
//
//        SharedPreferences prefs =
//                getSharedPreferences(getPackageName(), MODE_PRIVATE);
//
//        final String access_token = prefs.getString("access_token", "");
//
//        FileNameMap fileNameMap = URLConnection.getFileNameMap();
//
//        File photo = new File(filePath);
//
//        String mimeType = fileNameMap.getContentTypeFor(filePath);
//
//        SendFileTask sendFileTask = new SendFileTask(filePath, mimeType);
//
//        sendFileTask.execute("");
//
//    }

}