package com.viableindustries.waterreporter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.models.auth.RegistrationResponse;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PasswordResetActivity extends Activity {

    @Bind(R.id.email)
    EditText email_text;

    @Bind(R.id.reset_success)
    TextView reset_success;

    @Bind(R.id.reset_error)
    TextView reset_error;

    private Pattern emailPattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_password_reset);

        ButterKnife.bind(this);

        // Check to see if we can populate the email field with an account on device

        emailPattern = android.util.Patterns.EMAIL_ADDRESS;

//        Account[] accounts = AccountManager.get(this).getAccounts();
//
//        for (Account account : accounts) {
//
//            if (emailPattern.matcher(account.name).matches()) {
//
//                String possibleEmail = account.name;
//
//                email_text.setText(possibleEmail);
//
//            }
//        }

        //prevent sign-in window from being closed by clicking outside of it
        //this forces the user to actually sign-in
        this.setFinishOnTouchOutside(false);

    }

    public void requestReset(View view) {

        final SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        String email = String.valueOf(email_text.getText());

        Matcher matcher = emailPattern.matcher(email);

        if (matcher.matches()) {

            Map<String, String> resetBody = new HashMap<>();

            resetBody.put("email", email);

            RestClient.getSecurityService().reset(resetBody,
                    new CancelableCallback<RegistrationResponse>() {
                        @Override
                        public void onSuccess(RegistrationResponse registrationResponse,
                                            Response response) {

                            int statusCode = registrationResponse.getCode();

                            if (statusCode == 200) {

                                reset_error.setVisibility(View.GONE);

                                reset_success.setVisibility(View.VISIBLE);

                            } else {

                                reset_error.setVisibility(View.VISIBLE);

                            }

                        }

                        @Override
                        public void onFailure(RetrofitError error) {

                            error.printStackTrace();

                        }
                    });

        } else {

            email_text.setText("");

            email_text.setHint("Enter a valid email address");

        }

    }

    public void toLogIn(View v) {

        startActivity(new Intent(this, SignInActivity.class));

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        // Cancel all pending network requests

        CancelableCallback.cancelAll();

    }

}
