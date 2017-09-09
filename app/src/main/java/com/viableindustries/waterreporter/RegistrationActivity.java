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
import android.widget.Toast;

import com.viableindustries.waterreporter.data.AuthResponse;
import com.viableindustries.waterreporter.data.CancelableCallback;
import com.viableindustries.waterreporter.data.LogInBody;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.RegistrationBody;
import com.viableindustries.waterreporter.data.RegistrationResponse;
import com.viableindustries.waterreporter.data.SecurityService;
import com.viableindustries.waterreporter.data.UserBasicResponse;
import com.viableindustries.waterreporter.data.UserService;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

// Activity shown as dialog to handle first-time user registration.

public class RegistrationActivity extends AppCompatActivity {

    @Bind(R.id.password)
    EditText password_text;

    @Bind(R.id.email)
    EditText email_text;

    @Bind(R.id.error_message)
    LinearLayout error_message;

    @Bind(R.id.register)
    Button registrationButton;

    @Bind(R.id.spinner)
    ProgressBar progressBar;

    static final int REGISTRATION_REQUEST = 1;

    static final int LOGIN_REQUEST = 2;

    static final int PROFILE_REQUEST = 1;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private Pattern emailPattern;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration);

        ButterKnife.bind(this);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        emailPattern = android.util.Patterns.EMAIL_ADDRESS;

        //set custom title layout for window
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        //prevent sign-in window from being closed by clicking outside of it
        //this forces the user to actually sign-in
        this.setFinishOnTouchOutside(false);

    }

    public void saveAccount(View view) {

        RestAdapter restAdapter = SecurityService.restAdapter;

        final SecurityService securityService = restAdapter.create(SecurityService.class);

        final String password = String.valueOf(password_text.getText());

        // Check to make sure that the user's password is at least 6 characters long.

        if (password.length() < 6) {

            password_text.setText("");

            CharSequence text = "Password must be at least 6 characters long.";
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
            toast.show();

            return;

        }

        final String email = String.valueOf(email_text.getText());

        Matcher matcher = emailPattern.matcher(email);

        if (matcher.matches()) {

            registrationButton.setVisibility(View.GONE);

            progressBar.setVisibility(View.VISIBLE);

            RegistrationBody registrationBody = new RegistrationBody(email, password);

            securityService.register(registrationBody,
                    new CancelableCallback<RegistrationResponse>() {
                        @Override
                        public void onSuccess(RegistrationResponse registrationResponse,
                                            Response response) {

                            int responseCode = registrationResponse.getCode();

                            if (responseCode == 400) {

                                error_message.setVisibility(View.VISIBLE);

                                progressBar.setVisibility(View.GONE);

                                registrationButton.setVisibility(View.VISIBLE);

                            } else {

                                // Store user:id in global preferences file

                                int userId = registrationResponse.getUserId();

                                prefs.edit()
                                        .putInt("user_id", userId)
                                        .apply();

                                // Store user:id again in a separate preference that holds metadata
                                // about the authenticated user. This is necessary because we later
                                // build a valid User object when navigating to the authenticated users'
                                // profile via the navigation bar. In that situation, we need access
                                // to an ID for comparison against the one stored in the global preferences
                                // file in order to determine whether or not to display the account settings
                                // button. This failsafe is in place to account for the edge case in which
                                // a user quits the sign up process without completing their profile by
                                // tapping the back button while on the initial profile screen. This will
                                // allow that person to access Water Reporter and/or edit their profile.

                                final SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                                coreProfile.edit()
                                        .putInt("id", userId)
                                        .apply();

                                // Silently log-in with new credentials

                                LogInBody logInBody = new LogInBody(email, password, getString(R.string.response_type),
                                        getString(R.string.client_id), getString(R.string.redirect_uri),
                                        getString(R.string.scope), getString(R.string.state));

                                securityService.save(logInBody,
                                        new CancelableCallback<AuthResponse>() {
                                            @Override
                                            public void onSuccess(AuthResponse authResponse,
                                                                Response response) {

                                                progressBar.setVisibility(View.GONE);

                                                registrationButton.setVisibility(View.VISIBLE);

                                                // Store API access token

                                                prefs.edit().putString("access_token", "Bearer " + authResponse.getAccessToken()).apply();

                                                // Set flag confirming successful registration

                                                prefs.edit().putBoolean("clean_slate", true).apply();

                                                startActivity(new Intent(RegistrationActivity.this, ProfileBasicActivity.class));

                                                finish();

                                            }

                                            @Override
                                            public void onFailure(RetrofitError error) {

                                                progressBar.setVisibility(View.GONE);

                                                registrationButton.setVisibility(View.VISIBLE);

                                            }
                                        });

                            }


                        }

                        @Override
                        public void onFailure(RetrofitError error) {

                            progressBar.setVisibility(View.GONE);

                            registrationButton.setVisibility(View.VISIBLE);

                        }
                    });

        } else {

            email_text.setText("");

            email_text.setHint("Enter a valid email address");

        }

    }

    public void toLogIn(View v) {

        startActivityForResult(new Intent(this, SignInActivity.class), LOGIN_REQUEST);

        finish();

    }

    // Exit the application when user taps the system back button

    @Override
    public void onBackPressed() {

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

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

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

    }

}
