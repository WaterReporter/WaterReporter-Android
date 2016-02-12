package com.viableindustries.waterreporter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.viableindustries.waterreporter.data.AuthResponse;
import com.viableindustries.waterreporter.data.LogInBody;
import com.viableindustries.waterreporter.data.SecurityService;
import com.viableindustries.waterreporter.data.UserBasicResponse;
import com.viableindustries.waterreporter.data.UserService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Ryan Hamley on 10/28/14.
 * Activity shown as dialog to handle sign in functionality.
 */
public class SignInActivity extends Activity {

    @Bind(R.id.password) EditText password_text;

    @Bind(R.id.email) EditText email_text;

    @Bind(R.id.error_message) LinearLayout error_message;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private Pattern emailPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sign_in);

        ButterKnife.bind(this);

        emailPattern = android.util.Patterns.EMAIL_ADDRESS;

        //prevent sign-in window from being closed by clicking outside of it
        //this forces the user to actually sign-in
        this.setFinishOnTouchOutside(false);

    }

    public void saveAccount(View view){

        final RestAdapter restAdapter = SecurityService.restAdapter;

        final SecurityService securityService = restAdapter.create(SecurityService.class);

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        String password = String.valueOf(password_text.getText());

        String email = String.valueOf(email_text.getText());

        Matcher matcher = emailPattern.matcher(email);

        if(matcher.matches()){

            LogInBody logInBody = new LogInBody(email, password, getString(R.string.response_type),
                    "Ru8hamw7ixuCtsHs23Twf4UB12fyIijdQcLssqpd", "http://stg.waterreporter.org/authorize",
                    getString(R.string.scope), getString(R.string.state));

            securityService.save(logInBody,
                    new Callback<AuthResponse>() {
                        @Override
                        public void success(AuthResponse authResponse,
                                            Response response) {

                            String access_token = "Bearer " + authResponse.getAccessToken();

                            prefs.edit().putString("access_token", access_token).apply();

                            // Since the user may have arrived here after re-installing the app and
                            // bypassing the registration dialog, we need to check for the presence of
                            // a user id. If we don't have one stored, retrieve it via the UserService
                            // by including the token obtained just now upon successful log-in.

                            int user_id = prefs.getInt("user_id", 0);

                            if (user_id == 0) {

                                UserService userService = restAdapter.create(UserService.class);

                                userService.getUser(access_token, "application/json",
                                        new Callback<UserBasicResponse>() {
                                            @Override
                                            public void success(UserBasicResponse userBasicResponse,
                                                                Response response) {

                                                prefs.edit().putInt("user_id", userBasicResponse.getUserId()).apply();

                                                Intent intent = new Intent();

                                                setResult(RESULT_OK, intent);

                                                finish();

                                            }

                                            @Override
                                            public void failure(RetrofitError error) {

                                            }
                                        });

                            } else {

                                finish();

                            }

                        }

                        @Override
                        public void failure(RetrofitError error) {

                            Response response = error.getResponse();

                            if (response != null) {

                                int status = response.getStatus();

                                if (status == 403) {

                                    error_message.setVisibility(View.VISIBLE);

                                }

                            }

                        }
                    });

        } else {

            email_text.setText("");

            email_text.setHint("Enter a valid email address");

        }

    }

    public void toReset (View v) {

        startActivity(new Intent(this, PasswordResetActivity.class));

        finish();

    }

}
