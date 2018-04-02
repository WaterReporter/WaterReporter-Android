package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.snapshot.OrganizationSnapshot;
import com.viableindustries.waterreporter.user_interface.dialogs.OrganizationExtrasBottomSheetDialogFragment;
import com.viableindustries.waterreporter.utilities.CircleTransform;
import com.viableindustries.waterreporter.utilities.ModelStorage;
import com.viableindustries.waterreporter.utilities.PatternEditableBuilder;

import java.util.Arrays;
import java.util.regex.Pattern;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.picasso.transformations.BlurTransformation;

public class OrganizationProfileCardActivity extends AppCompatActivity {

    private RelativeLayout reportStat;

    private TextView reportCounter;

    private RelativeLayout actionStat;

    private TextView actionCounter;

    private RelativeLayout peopleStat;

    private TextView peopleCounter;

    private TextView organizationDescription;

    private ImageView organizationLogo;

    private ImageView headerCanvas;

    private ImageView logoView;

    private RelativeLayout extraActions;

    private ImageView extraActionsIconView;

    private int organizationId;

    private SharedPreferences mSharedPreferences;

    private SharedPreferences mCoreProfile;

    private Organization mOrganization;

    private OrganizationSnapshot mOrganizationSnapshot;

    private Resources mResources;

    private Context mContext;

    @Bind(R.id.scrollView)
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_profile_card);

        ButterKnife.bind(this);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        mSharedPreferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mCoreProfile = getSharedPreferences(getString(R.string.active_user_profile_key), MODE_PRIVATE);

        mResources = getResources();

        mContext = this;

        retrieveStoredOrganization();

    }

    private void retrieveStoredOrganization() {

        mOrganization = ModelStorage.getStoredOrganization(mSharedPreferences);

        try {

            organizationId = mOrganization.properties.id;

            addListViewHeader();

            retrieveStoredSnapshot();

        } catch (NullPointerException e1) {

            try {

                organizationId = mOrganization.id;

                addListViewHeader();

                retrieveStoredSnapshot();

            } catch (NullPointerException e2) {

                Log.v("NO-STORED-GROUP", e2.toString());

                startActivity(new Intent(this, MainActivity.class));

                finish();

            }

        }

    }

    private void retrieveStoredSnapshot() {

        mOrganizationSnapshot = ModelStorage.getStoredOrganizationSnapshot(mSharedPreferences);

        try {

            int postCount = mOrganizationSnapshot.posts;

            Log.d("stored--user--posts", postCount + "");

            setSnapshotData(mOrganizationSnapshot);

        } catch (NullPointerException e1) {

            startActivity(new Intent(this, MainActivity.class));

            finish();

        }

    }

    private void addListViewHeader() {

        if (scrollView != null) {

            LayoutInflater inflater = getLayoutInflater();

            ViewGroup header = (ViewGroup) inflater.inflate(R.layout.organization_profile_card_full, scrollView, false);

            // Set up white color filter for reversed Water Reporter logo

            logoView = (ImageView) header.findViewById(R.id.logo);

            logoView.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);

            extraActionsIconView = (ImageView) header.findViewById(R.id.extraActionsIconView);

            extraActionsIconView.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_ATOP);

            TextView organizationName = (TextView) header.findViewById(R.id.organizationName);

            organizationDescription = (TextView) header.findViewById(R.id.organizationDescription);

            organizationLogo = (ImageView) header.findViewById(R.id.organizationLogo);

            reportCounter = (TextView) header.findViewById(R.id.reportCount);

            actionCounter = (TextView) header.findViewById(R.id.actionCount);

            peopleCounter = (TextView) header.findViewById(R.id.peopleCount);

            reportStat = (RelativeLayout) header.findViewById(R.id.reportStat);

            actionStat = (RelativeLayout) header.findViewById(R.id.actionStat);

            peopleStat = (RelativeLayout) header.findViewById(R.id.peopleStat);

            String organizationDescriptionText = mOrganization.properties.description;
            String organizationNameText = mOrganization.properties.name;
            String organizationLogoUrl = mOrganization.properties.picture;

            organizationName.setText(organizationNameText);

            headerCanvas = (ImageView) header.findViewById(R.id.headerCanvas);

            String firstChar = mOrganization.properties.name.substring(0, 1).toLowerCase();

            String[] alphabet = {
                    "a", "b", "c", "d", "e", "f",
                    "g", "h", "i", "j", "k", "l",
                    "m", "n", "o", "p", "q", "r",
                    "s", "t", "u", "v", "w", "x",
                    "y", "z"};

            int charIdx = Arrays.asList(alphabet).indexOf(firstChar);

            String canvasPath = "default.jpg";

            if (0 <= charIdx && charIdx <= 5) {

                canvasPath = "a_f.jpg";

            } else if (6 <= charIdx && charIdx <= 11) {

                canvasPath = "g_l.jpg";

            } else if (12 <= charIdx && charIdx <= 17) {

                canvasPath = "m_r.jpg";

            } else if (18 <= charIdx && charIdx <= 25) {

                canvasPath = "s_z.jpg";

            }

            Picasso.with(mContext)
                    .load(String.format("https://media.waterreporter.org/placeholders/profiles/group/%s", canvasPath))
                    .transform(new BlurTransformation(mContext, 6, 1))
                    .into(headerCanvas);

            Picasso.with(this).load(organizationLogoUrl).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(organizationLogo);

            try {

                organizationDescription.setText(organizationDescriptionText);

                new PatternEditableBuilder().
                        addPattern(mContext, Pattern.compile("\\#(\\w+)"), ContextCompat.getColor(mContext, R.color.waterreporter_blue),
                                new PatternEditableBuilder.SpannableClickedListener() {
                                    @Override
                                    public void onSpanClicked(String text) {

                                        Intent intent = new Intent(mContext, TagProfileActivity.class);
                                        intent.putExtra("tag", text);
                                        mContext.startActivity(intent);

                                    }
                                }).into(organizationDescription);

            } catch (NullPointerException ne) {

                organizationDescription.setVisibility(View.GONE);

            }

            peopleStat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(mContext, OrganizationMembersActivity.class);

                    startActivity(intent);

                }
            });

            // Present extra actions dialog (bottom sheet)

            extraActions = (RelativeLayout) header.findViewById(R.id.extraActions);

            extraActions.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    ModelStorage.storeModel(mSharedPreferences, mOrganization, "stored_organization");

                    OrganizationExtrasBottomSheetDialogFragment organizationExtrasBottomSheetDialogFragment =
                            new OrganizationExtrasBottomSheetDialogFragment();

                    organizationExtrasBottomSheetDialogFragment.show(getSupportFragmentManager(), "organization-extras-dialog");

                }

            });

            // Add populated header view to parent layout

            scrollView.addView(header);

        }

    }

    private void setSnapshotData(OrganizationSnapshot organizationSnapshot) {

        String reportCountText = String.format("%s %s", String.valueOf(organizationSnapshot.posts),
                mResources.getQuantityString(R.plurals.post_label, organizationSnapshot.posts, organizationSnapshot.posts));
        reportCounter.setText(reportCountText);

        String actionCountText = String.format("%s %s", String.valueOf(organizationSnapshot.actions),
                mResources.getQuantityString(R.plurals.action_label, organizationSnapshot.actions, organizationSnapshot.actions));
        actionCounter.setText(actionCountText);

        String peopleCountText = String.format("%s %s", String.valueOf(organizationSnapshot.members),
                mResources.getQuantityString(R.plurals.member_label, organizationSnapshot.members, organizationSnapshot.members));
        peopleCounter.setText(peopleCountText);

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

        Picasso.with(this).cancelRequest(headerCanvas);

        Picasso.with(this).cancelRequest(organizationLogo);

        ButterKnife.unbind(this);

    }

}