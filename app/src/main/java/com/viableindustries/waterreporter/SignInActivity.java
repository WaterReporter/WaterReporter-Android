package com.viableindustries.waterreporter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by Ryan Hamley on 10/28/14.
 * Activity shown as dialog to handle sign in functionality.
 */
public class SignInActivity extends Activity {
    @InjectView(R.id.name) EditText user_name;
    @InjectView(R.id.email) EditText email_text;

    private static final String NAME_KEY = "user_name";
    private static final String EMAIL_KEY = "user_email";
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_sign_in);
        //set custom title layout for window
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        //prevent sign-in window from being closed by clicking outside of it
        //this forces the user to actually sign-in
        this.setFinishOnTouchOutside(false);
    }

    public void saveAccount(View view){
        ButterKnife.inject(this);

        SharedPreferences prefs =
                getSharedPreferences(getPackageName(), MODE_PRIVATE);

        String name = String.valueOf(user_name.getText());
        String email = String.valueOf(email_text.getText());

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);

        if(matcher.matches()){
            prefs.edit().putString(EMAIL_KEY, email).putString(NAME_KEY, name).apply();

            finish();
        } else {
            email_text.setText("");
            email_text.setHint("Enter a valid email address");
        }

    }

    public void openBrowser(View view){
        Uri dataStatementUri = Uri.parse(getString(R.string.data_statement_uri));
        Intent intent = new Intent(Intent.ACTION_VIEW, dataStatementUri);
        startActivity(intent);
    }

}
