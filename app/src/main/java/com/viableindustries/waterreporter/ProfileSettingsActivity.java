package com.viableindustries.waterreporter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ProfileSettingsActivity extends AppCompatActivity {

    @Bind(R.id.edit_profile)
    TextView editProfile;

    @Bind(R.id.manage_groups)
    TextView manageGroups;

    @Bind(R.id.report_bug)
    TextView reportBug;

    @Bind(R.id.log_out)
    TextView logOut;

//    @Bind(R.id.reportCount)
//    TextView reportCounter;

    private SharedPreferences prefs;

    private SharedPreferences coreProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_settings);

//        getActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);

    }

    public void logOut(View view) {

        // Clear stored token and user id values

        prefs.edit().putString("access_token", "")
                .putInt("user_id", 0).apply();

        // Clear stored active user profile

        coreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        coreProfile.edit().clear().apply();

        startActivity(new Intent(this, SignInActivity.class));

    }

    public void composeEmail(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {"support@waterreporter.org"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Water Reporter for Android");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

    }

}

