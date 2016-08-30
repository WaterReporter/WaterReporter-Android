package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationFeatureCollection;
import com.viableindustries.waterreporter.data.OrganizationMemberList;
import com.viableindustries.waterreporter.data.OrganizationService;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserGroupList;
import com.viableindustries.waterreporter.data.UserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class OrganizationMembersActivity extends AppCompatActivity {

    @Bind(R.id.list)
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_organization_members);

        ButterKnife.bind(this);

        ArrayList<User> users = OrganizationMemberList.getList();

        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User user1, User user2) {
                return user1.properties.last_name.compareTo(user2.properties.last_name);
            }
        });

        populateUsers(users);


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

    private void populateUsers(ArrayList<User> users) {

        final UserListAdapter adapter = new UserListAdapter(this, users, true);

        listView.setAdapter(adapter);

    }

}
