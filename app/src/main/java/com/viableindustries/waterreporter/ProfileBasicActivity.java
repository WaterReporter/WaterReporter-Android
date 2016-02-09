package com.viableindustries.waterreporter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.viableindustries.waterreporter.data.AuthResponse;
import com.viableindustries.waterreporter.data.LogInBody;
import com.viableindustries.waterreporter.data.RegistrationBody;
import com.viableindustries.waterreporter.data.RegistrationResponse;
import com.viableindustries.waterreporter.data.SecurityService;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserBasicResponse;
import com.viableindustries.waterreporter.data.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

// Activity shown as dialog to handle final step in first-time user registration.

public class ProfileBasicActivity extends Activity {

    @Bind(R.id.first_name)
    EditText first_name;

    @Bind(R.id.last_name)
    EditText last_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_basic);

        ButterKnife.bind(this);

        //prevent sign-in window from being closed by clicking outside of it
        //this forces the user to actually sign-in
        this.setFinishOnTouchOutside(false);

    }

    public void saveProfile(View view) {

        final String firstName = String.valueOf(first_name.getText());

        final String lastName = String.valueOf(last_name.getText());

        if (firstName.isEmpty() || lastName.isEmpty()) {

            CharSequence text = "Please enter your first and last names.";
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.show();

            return;

        }

        RestAdapter restAdapter = UserService.restAdapter;

        final UserService userService = restAdapter.create(UserService.class);

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        int id = prefs.getInt("user_id", 0);

        String token = prefs.getString("access_token", "");

        Map<String, String> userPatch = new HashMap<String, String>();

        userPatch.put("first_name", firstName);

        userPatch.put("last_name", lastName);

        userService.updateUser(token,
                "application/json",
                id,
                userPatch,
                new Callback<User>() {
                    @Override
                    public void success(User user,
                                        Response response) {

                        startActivity(new Intent(ProfileBasicActivity.this, RegistrationGroupsActivity.class));

                        finish();


                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });


    }


}
