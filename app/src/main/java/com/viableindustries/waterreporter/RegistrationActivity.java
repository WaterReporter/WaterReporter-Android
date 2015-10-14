package com.viableindustries.waterreporter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.viableindustries.waterreporter.data.AuthResponse;
import com.viableindustries.waterreporter.data.LogInBody;
import com.viableindustries.waterreporter.data.RegistrationBody;
import com.viableindustries.waterreporter.data.RegistrationResponse;
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

// Activity shown as dialog to handle first-time user registration.

public class RegistrationActivity extends Activity {

    @Bind(R.id.password)
    EditText password_text;

    @Bind(R.id.email)
    EditText email_text;

    @Bind(R.id.register)
    Button registrationButton;

    @Bind(R.id.spinner)
    ProgressBar progressBar;

    static final int PROFILE_REQUEST = 1;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private Pattern emailPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        setContentView(R.layout.activity_registration);

        ButterKnife.bind(this);

        // Check to see if we can populate the email field with an account on device

        emailPattern = android.util.Patterns.EMAIL_ADDRESS;

        Account[] accounts = AccountManager.get(this).getAccounts();

        for (Account account : accounts) {

            if (emailPattern.matcher(account.name).matches()) {

                String possibleEmail = account.name;

                email_text.setText(possibleEmail);

            }
        }

        //set custom title layout for window
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        //prevent sign-in window from being closed by clicking outside of it
        //this forces the user to actually sign-in
        this.setFinishOnTouchOutside(false);

    }

    public void saveAccount(View view) {

        RestAdapter restAdapter = SecurityService.restAdapter;

        final SecurityService securityService = restAdapter.create(SecurityService.class);

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

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
                    new Callback<RegistrationResponse>() {
                        @Override
                        public void success(RegistrationResponse registrationResponse,
                                            Response response) {

                            // Store user id
                            prefs.edit()
                                    .putInt("user_id", registrationResponse.getUserId())
                                    .apply();

                            // Silently log-in with new credentials
                            LogInBody logInBody = new LogInBody(email, password, getString(R.string.response_type),
                                    getString(R.string.client_id), getString(R.string.redirect_uri),
                                    getString(R.string.scope), getString(R.string.state));

                            securityService.save(logInBody,
                                    new Callback<AuthResponse>() {
                                        @Override
                                        public void success(AuthResponse authResponse,
                                                            Response response) {

                                            progressBar.setVisibility(View.GONE);

                                            registrationButton.setVisibility(View.VISIBLE);

                                            // Store API access token
                                            prefs.edit().putString("access_token", "Bearer " + authResponse.getAccessToken()).apply();

                                            startActivity(new Intent(RegistrationActivity.this, ProfileBasicActivity.class));

                                            finish();

                                        }

                                        @Override
                                        public void failure(RetrofitError error) {

                                            progressBar.setVisibility(View.GONE);

                                            registrationButton.setVisibility(View.VISIBLE);

                                        }
                                    });


                        }

                        @Override
                        public void failure(RetrofitError error) {

                            progressBar.setVisibility(View.GONE);

                            registrationButton.setVisibility(View.VISIBLE);

                        }
                    });

        } else {

            email_text.setText("");

            email_text.setHint("Enter a valid email address");

        }

    }

    public void existingAccount(View v) {

        final RestAdapter restAdapter = SecurityService.restAdapter;

        final SecurityService securityService = restAdapter.create(SecurityService.class);

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        String password = String.valueOf(password_text.getText());

        String email = String.valueOf(email_text.getText());

        if (password.isEmpty() || email.isEmpty()) {

            startActivity(new Intent(this, SignInActivity.class));

            finish();

        }

        Matcher matcher = emailPattern.matcher(email);

        if (matcher.matches()) {

            registrationButton.setVisibility(View.GONE);

            progressBar.setVisibility(View.VISIBLE);

            LogInBody logInBody = new LogInBody(email, password, getString(R.string.response_type),
                    getString(R.string.client_id), getString(R.string.redirect_uri),
                    getString(R.string.scope), getString(R.string.state));

            securityService.save(logInBody,
                    new Callback<AuthResponse>() {
                        @Override
                        public void success(AuthResponse authResponse,
                                            Response response) {

                            String access_token = "Bearer " + authResponse.getAccessToken();

                            prefs.edit().putString("access_token", access_token).apply();

                            // This method was triggered by the user successfully completing both the email
                            // and password fields, and then tapping "I already have an account" rather than
                            // "Join". Because we're in the Registration context, we don't have a user id in
                            // storage and therefore need to retrieve it via the UserService by including
                            // the token obtained just now upon successful log-in.

                            // Keep in mind that users could be re-installing the app or setting up a new device.

                            UserService userService = restAdapter.create(UserService.class);

                            userService.getUser(access_token, "application/json",
                                    new Callback<UserBasicResponse>() {
                                        @Override
                                        public void success(UserBasicResponse userBasicResponse,
                                                            Response response) {

                                            progressBar.setVisibility(View.GONE);

                                            registrationButton.setVisibility(View.VISIBLE);

                                            prefs.edit().putInt("user_id", userBasicResponse.getUserId()).apply();

                                            Intent intent = new Intent();

                                            setResult(RESULT_OK, intent);

                                            finish();

                                        }

                                        @Override
                                        public void failure(RetrofitError error) {

                                            progressBar.setVisibility(View.GONE);

                                            registrationButton.setVisibility(View.VISIBLE);

                                        }

                                    });

                        }

                        @Override
                        public void failure(RetrofitError error) {

                            progressBar.setVisibility(View.GONE);

                            registrationButton.setVisibility(View.VISIBLE);

                        }

                    });

        } else {

            email_text.setText("");

            email_text.setHint("Enter a valid email address");

        }

    }

}
