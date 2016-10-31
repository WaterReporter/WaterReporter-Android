package com.viableindustries.waterreporter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.viableindustries.waterreporter.data.AuthResponse;
import com.viableindustries.waterreporter.data.LogInBody;
import com.viableindustries.waterreporter.data.SecurityService;
import com.viableindustries.waterreporter.data.User;
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
public class SignInActivity extends AppCompatActivity {

    @Bind(R.id.password)
    EditText password_text;

    @Bind(R.id.email)
    EditText email_text;

    @Bind(R.id.error_message)
    LinearLayout error_message;

    @Bind(R.id.log_in)
    Button logInButton;

    @Bind(R.id.spinner)
    ProgressBar progressBar;

    static final int REGISTRATION_REQUEST = 1;

    static final int LOGIN_REQUEST = 2;

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

    protected void onRequestError(RetrofitError error) {

        logInButton.setVisibility(View.VISIBLE);

        progressBar.setVisibility(View.GONE);

        Response response = error.getResponse();

        if (response != null) {

            int status = response.getStatus();

            if (status == 403) {

                error_message.setVisibility(View.VISIBLE);

                password_text.getText().clear();

            }

        }

    }

    public void saveAccount(View view) {

        final RestAdapter restAdapter = SecurityService.restAdapter;

        final SecurityService securityService = restAdapter.create(SecurityService.class);

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        String password = String.valueOf(password_text.getText());

        String email = String.valueOf(email_text.getText());

        Matcher matcher = emailPattern.matcher(email);

        if (matcher.matches()) {

            logInButton.setVisibility(View.GONE);

            progressBar.setVisibility(View.VISIBLE);

            LogInBody logInBody = new LogInBody(email, password, getString(R.string.response_type),
                    "Ru8hamw7ixuCtsHs23Twf4UB12fyIijdQcLssqpd", "http://stg.waterreporter.org/authorize",
                    getString(R.string.scope), getString(R.string.state));

            securityService.save(logInBody,
                    new Callback<AuthResponse>() {
                        @Override
                        public void success(AuthResponse authResponse,
                                            Response response) {

                            final String access_token = "Bearer " + authResponse.getAccessToken();

                            prefs.edit().putString("access_token", access_token).apply();

                            // Since the user may have arrived here after re-installing the app and
                            // bypassing the registration dialog, we need to check for the presence of
                            // a user id. If we don't have one stored, retrieve it via the UserService
                            // by including the token obtained just now upon successful log-in.

                            int user_id = prefs.getInt("user_id", 0);

                            if (user_id == 0) {

                                final UserService userService = restAdapter.create(UserService.class);

                                userService.getActiveUser(access_token, "application/json",
                                        new Callback<UserBasicResponse>() {
                                            @Override
                                            public void success(UserBasicResponse userBasicResponse,
                                                                Response response) {

                                                prefs.edit().putInt("user_id", userBasicResponse.getUserId()).apply();

                                                userService.getUser(access_token,
                                                        "application/json",
                                                        userBasicResponse.getUserId(),
                                                        new Callback<User>() {
                                                            @Override
                                                            public void success(User user,
                                                                                Response response) {

                                                                final SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                                                                coreProfile.edit()
                                                                        //.putBoolean("active", user.properties.active)
                                                                        .putInt("id", user.id)
                                                                        .putString("picture", user.properties.images.get(0).properties.icon_retina)
                                                                        .apply();

                                                                // Model strings
                                                                String[] KEYS = {"description", "first_name",
                                                                        "last_name", "organization_name", //"picture",
                                                                        "public_email", "title"};

                                                                for (String key : KEYS) {

                                                                    coreProfile.edit().putString(key, user.properties.getStringProperties().get(key)).apply();

                                                                }

                                                                coreProfile.edit().putString("role", user.properties.roles.get(0).properties.name).apply();

                                                                //Intent intent = new Intent();

                                                                //setResult(RESULT_OK, intent);

                                                                //finish();

                                                                startActivity(new Intent(SignInActivity.this, MainActivity.class));

                                                            }

                                                            @Override
                                                            public void failure(RetrofitError error) {
                                                                //
                                                                onRequestError(error);

                                                            }
                                                        });

                                            }

                                            @Override
                                            public void failure(RetrofitError error) {
                                                //
                                                onRequestError(error);

                                            }
                                        });

                            } else {

                                finish();

                            }

                        }

                        @Override
                        public void failure(RetrofitError error) {

                            onRequestError(error);

                        }
                    });

        } else {

            email_text.setText("");

            email_text.setHint("Enter a valid email address");

        }

    }

    public void toReset(View v) {

        startActivity(new Intent(this, PasswordResetActivity.class));

        finish();

    }

    public void toRegister(View v) {

        startActivityForResult(new Intent(this, RegistrationActivity.class), REGISTRATION_REQUEST);

        finish();

    }

    // Exit the application when user taps the system back button

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            // The user is logged in and may already have reports in the system.
            // Let's attempt to fetch the user's report collection and, if none exist,
            // direct the user to submit their first report.
            startActivity(new Intent(this, MainActivity.class));

        }

    }

}
