package com.viableindustries.waterreporter.user_interface.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.viableindustries.waterreporter.OrganizationCampaignsActivity;
import com.viableindustries.waterreporter.OrganizationMembersActivity;
import com.viableindustries.waterreporter.OrganizationProfileCardActivity;
import com.viableindustries.waterreporter.OrganizationWatershedsActivity;
import com.viableindustries.waterreporter.R;

/**
 * Created by brendanmcintyre on 3/26/18.
 */

public class OrganizationExtrasBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }

    };

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        BottomSheetDialog dialog =
                new BottomSheetDialog(getActivity());

        dialog.setContentView(R.layout.organization_extras_bottom_sheet);

        LinearLayout viewFullProfile = (LinearLayout) dialog.findViewById(R.id.viewFullProfile);
        LinearLayout viewMembers = (LinearLayout) dialog.findViewById(R.id.viewMembers);
        LinearLayout viewCampaigns = (LinearLayout) dialog.findViewById(R.id.viewCampaigns);
        LinearLayout viewWatersheds = (LinearLayout) dialog.findViewById(R.id.viewWatersheds);

        if (viewFullProfile != null &&
                viewMembers != null &&
                viewCampaigns != null &&
                viewWatersheds != null) {

            try {

                boolean profileContext = getArguments().getBoolean("profile_context", false);

                if (!profileContext) {

                    viewFullProfile.setVisibility(View.GONE);

                } else {

                    viewFullProfile.setOnClickListener(this);

                }

            } catch (NullPointerException e) {

                viewFullProfile.setVisibility(View.GONE);

            }

            viewMembers.setOnClickListener(this);
            viewCampaigns.setOnClickListener(this);
            viewWatersheds.setOnClickListener(this);

        } else {

            dismiss();

        }

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;

                FrameLayout bottomSheet = (FrameLayout) d.findViewById(android.support.design.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        //

        return dialog;

    }

    @Override
    public void onClick(View view) {

        final Context context = getContext();

        Log.d("dialog-view-id", "" + view.getId());

        switch (view.getId()) {

            case R.id.viewFullProfile:

                context.startActivity(new Intent(context, OrganizationProfileCardActivity.class));

                dismiss();

                break;

            case R.id.viewMembers:

                context.startActivity(new Intent(context, OrganizationMembersActivity.class));

                dismiss();

                break;

            case R.id.viewCampaigns:

                context.startActivity(new Intent(context, OrganizationCampaignsActivity.class));

                dismiss();

                break;

            case R.id.viewWatersheds:

                context.startActivity(new Intent(context, OrganizationWatershedsActivity.class));

                dismiss();

                break;

            default:

                break;

        }

    }

}
