package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.favorite.Favorite;
import com.viableindustries.waterreporter.api.models.favorite.FavoriteCollection;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportHolder;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.user_interface.adapters.UserListAdapter;
import com.viableindustries.waterreporter.utilities.CancelableCallback;
import com.viableindustries.waterreporter.utilities.EndlessScrollListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PostFavoriteListActivity extends AppCompatActivity {

    @Bind(R.id.memberListContainer)
    SwipeRefreshLayout memberListContainer;

    @Bind(R.id.memberList)
    ListView memberList;

    @Bind(R.id.backArrow)
    RelativeLayout backButton;

    private Context mContext;

    private Report mPost;

    private SharedPreferences mSharedPreferences;

    private List<User> memberCollection = new ArrayList<>();

    private UserListAdapter userListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_favorite_list);

        ButterKnife.bind(this);

        mContext = this;

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Set refresh listener on report feed container

        memberListContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i("fresh", "onRefresh called from SwipeRefreshLayout");
                        // This method performs the actual api-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        fetchFavorites(100, 1, mPost.id, true);
                    }
                }
        );

        // Set color of swipe refresh arrow animation

        memberListContainer.setColorSchemeResources(R.color.waterreporter_blue);

        memberListContainer.setRefreshing(true);

        // Retrieve stored Post

        retrieveStoredPost();

    }

    private void retrieveStoredPost(){

        mPost = ReportHolder.getReport();

        try {

            int postId = mPost.properties.id;

        } catch (NullPointerException e) {

            mPost = ModelStorage.getStoredPost(mSharedPreferences);

            try {

                int postId = mPost.properties.id;

                fetchFavorites(100, 1, mPost.id, true);

            } catch (NullPointerException _e) {

                startActivity(new Intent(this, MainActivity.class));

                finish();

            }

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateUsers(List<User> users) {

        userListAdapter = new UserListAdapter(this, users);

        memberList.setAdapter(userListAdapter);

        attachScrollListener();

    }

    private void attachScrollListener() {

        memberList.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new api needs to be appended to the list

                fetchFavorites(100, page, mPost.id, false);

                return true; // ONLY if more api is actually being loaded; false otherwise.

            }

        });

    }

    private void fetchFavorites(int limit, int page, int postId, final boolean refresh) {

        final SharedPreferences mSharedPreferences =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = mSharedPreferences.getString("access_token", "");

        RestClient.getReportService().getPostLikes(accessToken, "application/json", postId, page, limit, null, new CancelableCallback<FavoriteCollection>() {

            @Override
            public void onSuccess(FavoriteCollection favoriteCollection, Response response) {

                ArrayList<Favorite> favorites = favoriteCollection.getFeatures();

                if (!favorites.isEmpty()) {

                    if (refresh) {

                        memberCollection = new ArrayList<>();

                        for (Favorite favorite : favorites) {

                            memberCollection.add(favorite.properties.owner);

                        }

                        populateUsers(memberCollection);

                    } else {

                        for (Favorite favorite : favorites) {

                            memberCollection.add(favorite.properties.owner);

                        }

                        userListAdapter.notifyDataSetChanged();

                    }

                }

                memberListContainer.setRefreshing(false);

            }

            @Override
            public void onFailure(RetrofitError error) {

                memberListContainer.setRefreshing(false);

                if (error == null) return;

                Response errorResponse = error.getResponse();

                // If we have a valid response object, check the status code and redirect to log in view if necessary

                if (errorResponse != null) {

                    int status = errorResponse.getStatus();

                    if (status == 403) {

                        startActivity(new Intent(mContext, SignInActivity.class));

                    }

                }

            }

        });

    }

    @Override
    public void onResume() {

        super.onResume();

        // Retrieve stored Post

        retrieveStoredPost();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

    }

}