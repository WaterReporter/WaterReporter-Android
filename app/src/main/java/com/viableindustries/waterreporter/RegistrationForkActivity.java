package com.viableindustries.waterreporter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;

// Activity shown as dialog to handle first-time user registration.

public class RegistrationForkActivity extends Activity {

    @Bind(R.id.register)
    Button register;

    @Bind(R.id.login)
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_registration_fork);

        ButterKnife.bind(this);

        //prevent sign-in window from being closed by clicking outside of it
        //this forces the user to actually sign-in
        this.setFinishOnTouchOutside(false);

    }

    public void toRegistration (View view) {

        startActivity(new Intent(this, RegistrationActivity.class));

        finish();

    }

    public void toLoginIn (View view) {

        startActivity(new Intent(this, SignInActivity.class));

        finish();

    }

}
