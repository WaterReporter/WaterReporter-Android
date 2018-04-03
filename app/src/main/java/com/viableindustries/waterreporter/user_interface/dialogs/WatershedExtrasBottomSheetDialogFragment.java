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

import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.WatershedCampaignsActivity;
import com.viableindustries.waterreporter.WatershedGroupsActivity;
import com.viableindustries.waterreporter.WatershedUsersActivity;

/**
 * Created by brendanmcintyre on 4/2/18.
 */

public class WatershedExtrasBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

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

        dialog.setContentView(R.layout.watershed_extras_bottom_sheet);

        LinearLayout viewGroups = (LinearLayout) dialog.findViewById(R.id.viewGroups);
        LinearLayout viewCampaigns = (LinearLayout) dialog.findViewById(R.id.viewCampaigns);
        LinearLayout viewUsers = (LinearLayout) dialog.findViewById(R.id.viewUsers);

        if (viewGroups != null &&
                viewCampaigns != null &&
                viewUsers != null) {

            viewGroups.setOnClickListener(this);
            viewCampaigns.setOnClickListener(this);
            viewUsers.setOnClickListener(this);

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

            case R.id.viewGroups:

                context.startActivity(new Intent(context, WatershedGroupsActivity.class));

                dismiss();

                break;

            case R.id.viewCampaigns:

                context.startActivity(new Intent(context, WatershedCampaignsActivity.class));

                dismiss();

                break;

            case R.id.viewUsers:

                context.startActivity(new Intent(context, WatershedUsersActivity.class));

                dismiss();

                break;

            default:

                break;

        }

    }

}
