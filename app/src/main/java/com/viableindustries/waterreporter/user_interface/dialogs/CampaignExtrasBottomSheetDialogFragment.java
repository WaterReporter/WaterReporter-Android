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

import com.viableindustries.waterreporter.CampaignGroupsActivity;
import com.viableindustries.waterreporter.CampaignMembersActivity;
import com.viableindustries.waterreporter.CampaignWatershedsActivity;
import com.viableindustries.waterreporter.PhotoMetaActivity;
import com.viableindustries.waterreporter.R;

/**
 * Created by brendanmcintyre on 3/26/18.
 */

public class CampaignExtrasBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

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

        dialog.setContentView(R.layout.campaign_extras_bottom_sheet);

        LinearLayout startPost = (LinearLayout) dialog.findViewById(R.id.startCampaignPost);
        LinearLayout viewGroups = (LinearLayout) dialog.findViewById(R.id.viewGroups);
        LinearLayout viewMembers = (LinearLayout) dialog.findViewById(R.id.viewMembers);
        LinearLayout viewWatersheds = (LinearLayout) dialog.findViewById(R.id.viewWatersheds);

        if (startPost != null &&
                viewGroups != null &&
                viewMembers != null &&
                viewWatersheds != null) {

            startPost.setOnClickListener(this);
            viewGroups.setOnClickListener(this);
            viewMembers.setOnClickListener(this);
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

            case R.id.startCampaignPost:

                context.startActivity(new Intent(context, PhotoMetaActivity.class));

                dismiss();

                break;

            case R.id.viewMembers:

                context.startActivity(new Intent(context, CampaignMembersActivity.class));

                dismiss();

                break;

            case R.id.viewGroups:

                context.startActivity(new Intent(context, CampaignGroupsActivity.class));

                dismiss();

                break;

            case R.id.viewWatersheds:

                context.startActivity(new Intent(context, CampaignWatershedsActivity.class));

                dismiss();

                break;

            default:

                break;

        }

    }

}
