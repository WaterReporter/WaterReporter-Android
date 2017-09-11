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

import com.viableindustries.waterreporter.data.CancelableCallback;
import com.viableindustries.waterreporter.data.Favorite;
import com.viableindustries.waterreporter.data.FavoriteCollection;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportService;
import com.viableindustries.waterreporter.data.User;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PostFavoriteListActivity extends AppCompatActivity {

    @Bind(R.id.memberListContainer)
    private final
    SwipeRefreshLayout memberListContainer;

    @Bind(R.id.memberList)
    private final
    ListView memberList;

    @Bind(R.id.backArrow)
    private final
    RelativeLayout backButton;

    private Context mContext;

    private Report post;

    private List<User> memberCollection = new ArrayList<>();

    private UserListAdapter userListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_favorite_list);

        ButterKnife.bind(this);

        mContext = this;

        post = ReportHolder.getReport();

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
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        fetchFavorites(100, 1, post.id, true);
                    }
                }
        );

        // Set color of swipe refresh arrow animation

        memberListContainer.setColorSchemeResources(R.color.waterreporter_blue);

        memberListContainer.setRefreshing(true);

        fetchFavorites(100, 1, post.id, true);

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

        userListAdapter = new UserListAdapter(this, users, true);

        memberList.setAdapter(userListAdapter);

        attachScrollListener();

    }

    private void attachScrollListener() {

        memberList.setOnScrollListener(new EndlessScrollListener() {

            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {

                // Triggered only when new data needs to be appended to the list

                fetchFavorites(100, page, post.id, false);

                return true; // ONLY if more data is actually being loaded; false otherwise.

            }

        });

    }

    private void fetchFavorites(int limit, int page, int postId, final boolean refresh) {

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        final String accessToken = prefs.getString("access_token", "");

        Log.d("", accessToken);

        ReportService service = ReportService.restAdapter.create(ReportService.class);

        service.getPostLikes(accessToken, "application/json", postId, page, limit, null, new CancelableCallback<FavoriteCollection>() {

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
