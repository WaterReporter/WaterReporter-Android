package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.SignInActivity;
import com.viableindustries.waterreporter.api.interfaces.RestClient;
import com.viableindustries.waterreporter.api.interfaces.data.user.UserService;
import com.viableindustries.waterreporter.api.models.notification.NotificationPatchBody;
import com.viableindustries.waterreporter.api.models.notification.NotificationSetting;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.utilities.CancelableCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by brendanmcintyre on 11/15/16.
 */

public class NotificationSettingAdapter extends ArrayAdapter {

    private final Context mContext;

    public NotificationSettingAdapter(Context aContext, List features) {
        super(aContext, 0, features);
        this.mContext = aContext;
    }

    static class ViewHolder {
        TextView settingDescription;
        SwitchCompat settingControl;
        TextView tracker;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final NotificationSettingAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.notification_control, parent, false);

            viewHolder = new NotificationSettingAdapter.ViewHolder();

            viewHolder.settingDescription = (TextView) convertView.findViewById(R.id.setting_description);
            viewHolder.settingControl = (SwitchCompat) convertView.findViewById(R.id.setting_control);
            viewHolder.tracker = (TextView) convertView.findViewById(R.id.tracker);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (NotificationSettingAdapter.ViewHolder) convertView.getTag();

        }

        // Get the api item for this position
        final NotificationSetting setting = (NotificationSetting) getItem(position);

        viewHolder.settingDescription.setText(setting != null ? setting.description : null);

        viewHolder.tracker.setText(setting != null ? setting.name : null);

        viewHolder.settingControl.setChecked(setting.value);

        viewHolder.settingControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                CharSequence text = "Updating notification setting...";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(mContext, text, duration);
                toast.show();

                final SharedPreferences coreProfile = mContext.getSharedPreferences(mContext.getString(R.string.active_user_profile_key), MODE_PRIVATE);

                final SharedPreferences prefs = mContext.getSharedPreferences(mContext.getPackageName(), 0);

                // Retrieve API token

                final String accessToken = prefs.getString("access_token", "");

                // Retrieve user ID

                int id = prefs.getInt("user_id", 0);

                // Prepare request object

                Map<String, Boolean> settingPatch = new HashMap<>();

                settingPatch.put(viewHolder.tracker.getText().toString(), viewHolder.settingControl.isChecked());

                NotificationPatchBody notificationPatchBody = new NotificationPatchBody(settingPatch);

                RestClient.getUserService().updateUser(accessToken, "application/json", id, notificationPatchBody.settings, new CancelableCallback<User>() {

                    @Override
                    public void onSuccess(User user, Response response) {

                        // Update stored values of user's notification settings

                        Map<String, Boolean> userNotificationSettings = user.properties.getNotificationProperties();

                        for (Map.Entry<String, Boolean> entry : userNotificationSettings.entrySet()) {

                            coreProfile.edit().putBoolean(entry.getKey(), entry.getValue()).apply();

                        }

                        CharSequence text = "Notification setting successfully changed.";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(mContext, text, duration);
                        toast.show();

                    }

                    @Override
                    public void onFailure(RetrofitError error) {

                        if (error == null) return;

                        Response errorResponse = error.getResponse();

                        // If we have a valid response object, check the status code and redirect to log in view if necessary

                        if (errorResponse != null) {

                            int status = errorResponse.getStatus();

                            if (status == 403) {

                                mContext.startActivity(new Intent(mContext, SignInActivity.class));

                            }

                        }

                    }

                });

            }

        });

        // Return the completed view to render on screen
        return convertView;

    }

}
