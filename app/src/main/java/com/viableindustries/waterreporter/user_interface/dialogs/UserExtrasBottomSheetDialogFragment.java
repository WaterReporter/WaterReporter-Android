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
import com.viableindustries.waterreporter.UserProfileCardActivity;

/**
 * Created by brendanmcintyre on 3/26/18.
 */

public class UserExtrasBottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

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

        dialog.setContentView(R.layout.user_extras_bottom_sheet);

        LinearLayout viewFullProfile = (LinearLayout) dialog.findViewById(R.id.viewGroups);

        if (viewFullProfile != null) {

            viewFullProfile.setOnClickListener(this);

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

                context.startActivity(new Intent(context, UserProfileCardActivity.class));

                dismiss();

                break;

            default:

                break;

        }

    }

}