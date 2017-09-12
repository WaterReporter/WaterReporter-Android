package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.auth.AuthResponse;
import com.viableindustries.waterreporter.api.models.auth.LogInBody;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.api.models.user.UserBasicResponse;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
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

    private static final int REGISTRATION_REQUEST = 1;

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

        SharedPreferences prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        emailPattern = android.util.Patterns.EMAIL_ADDRESS;

        //prevent sign-in window from being closed by clicking outside of it
        //this forces the user to actually sign-in
        this.setFinishOnTouchOutside(false);

    }

    private void onRequestError(RetrofitError error) {

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

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        String password = String.valueOf(password_text.getText());

        String email = String.valueOf(email_text.getText());

        Matcher matcher = emailPattern.matcher(email);

        if (matcher.matches()) {

            logInButton.setVisibility(View.GONE);

            progressBar.setVisibility(View.VISIBLE);

            LogInBody logInBody = new LogInBody(email, password, getString(R.string.response_type),
                    getString(R.string.client_id), getString(R.string.redirect_uri),
                    getString(R.string.scope), getString(R.string.state));

            RestClient.getSecurityService().save(logInBody,
                    new CancelableCallback<AuthResponse>() {
                        @Override
                        public void onSuccess(AuthResponse authResponse,
                                            Response response) {

                            final String accessToken = "Bearer " + authResponse.getAccessToken();

                            prefs.edit().putString("access_token", accessToken).apply();

                            // Since the user may have arrived here after re-installing the app and
                            // bypassing the registration dialog, we need to check for the presence of
                            // a user id. If we don't have one stored, retrieve it via the UserService
                            // by including the token obtained just now upon successful log-in.

                            int user_id = prefs.getInt("user_id", 0);

                            if (user_id == 0) {

                                RestClient.getUserService().getActiveUser(accessToken, "application/json",
                                        new CancelableCallback<UserBasicResponse>() {
                                            @Override
                                            public void onSuccess(UserBasicResponse userBasicResponse,
                                                                Response response) {

                                                prefs.edit().putInt("user_id", userBasicResponse.getUserId()).apply();

                                                RestClient.getUserService().getUser(accessToken,
                                                        "application/json",
                                                        userBasicResponse.getUserId(),
                                                        new CancelableCallback<User>() {
                                                            @Override
                                                            public void onSuccess(User user,
                                                                                Response response) {

                                                                // Set flag confirming successful sign-in

                                                                prefs.edit().putBoolean("clean_slate", true).apply();

                                                                final SharedPreferences coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

                                                                coreProfile.edit()
                                                                        .putInt("id", user.id)
                                                                        .apply();

                                                                // Update stored values of user's string type attributes

                                                                Map<String, String> userStringProperties = user.properties.getStringProperties();

                                                                for (Map.Entry<String, String> entry : userStringProperties.entrySet()) {

                                                                    coreProfile.edit().putString(entry.getKey(), entry.getValue()).apply();

                                                                }

                                                                // Update stored values of user's notification settings

                                                                Map<String, Boolean> userNotificationSettings = user.properties.getNotificationProperties();

                                                                for (Map.Entry<String, Boolean> entry : userNotificationSettings.entrySet()) {

                                                                    coreProfile.edit().putBoolean(entry.getKey(), entry.getValue()).apply();

                                                                }

                                                                // Update stored values of user's group memberships

                                                                final SharedPreferences groupPrefs = getSharedPreferences(getString(R.string.group_membership_key), 0);

                                                                for (Organization organization : user.properties.organizations) {

                                                                    groupPrefs.edit().putInt(organization.properties.name, organization.properties.id).apply();

                                                                }

                                                                // Update stored values of user's role designation

                                                                coreProfile.edit().putString("role", user.properties.roles.get(0).properties.name).apply();

                                                                startActivity(new Intent(SignInActivity.this, MainActivity.class));

                                                            }

                                                            @Override
                                                            public void onFailure(RetrofitError error) {
                                                                //
                                                                onRequestError(error);

                                                            }
                                                        });

                                            }

                                            @Override
                                            public void onFailure(RetrofitError error) {
                                                //
                                                onRequestError(error);

                                            }
                                        });

                            } else {

                                finish();

                            }

                        }

                        @Override
                        public void onFailure(RetrofitError error) {

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