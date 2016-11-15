package com.viableindustries.waterreporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Comment;
import com.viableindustries.waterreporter.data.NotificationPatchBody;
import com.viableindustries.waterreporter.data.NotificationSetting;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.viableindustries.waterreporter.data.User;
import com.viableindustries.waterreporter.data.UserProfileListener;
import com.viableindustries.waterreporter.data.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by brendanmcintyre on 11/15/16.
 */

public class NotificationSettingAdapter extends ArrayAdapter {

    private final Context context;

    public NotificationSettingAdapter(Context context, List features, boolean isProfile) {
        super(context, 0, features);
        this.context = context;
    }

    protected static class ViewHolder {
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

        // Get the data item for this position
        final NotificationSetting setting = (NotificationSetting) getItem(position);

        viewHolder.settingDescription.setText(setting.description);

        viewHolder.tracker.setText(setting.name);

        viewHolder.settingControl.setChecked(setting.value);

        viewHolder.settingControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                CharSequence text = "Updating notification setting...";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                final SharedPreferences coreProfile = context.getSharedPreferences(context.getString(R.string.active_user_profile_key), MODE_PRIVATE);

                final SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), 0);

                // Retrieve API token

                final String access_token = prefs.getString("access_token", "");

                // Retrieve user ID

                int id = prefs.getInt("user_id", 0);

                // Prepare request object

                Map<String, Boolean> settingPatch = new HashMap<String, Boolean>();

                settingPatch.put(viewHolder.tracker.getText().toString(), viewHolder.settingControl.isChecked());

                NotificationPatchBody notificationPatchBody = new NotificationPatchBody(settingPatch);

                // Instantiate API service

                UserService service = UserService.restAdapter.create(UserService.class);

                service.updateUser(access_token, "application/json", id, notificationPatchBody.settings, new Callback<User>() {

                    @Override
                    public void success(User user, Response response) {

                        // Update stored values of user's notification settings

                        Map<String, Boolean> userNotificationSettings = user.properties.getNotificationProperties();

                        for (Map.Entry<String, Boolean> entry : userNotificationSettings.entrySet()) {

                            coreProfile.edit().putBoolean(entry.getKey(), entry.getValue()).apply();

                        }

//                        viewHolder.settingControl.setChecked(!viewHolder.settingControl.isChecked());

                        CharSequence text = "Notification setting successfully changed.";
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();

                    }

                    @Override
                    public void failure(RetrofitError error) {

                        if (error == null) return;

                        Response errorResponse = error.getResponse();

                        // If we have a valid response object, check the status code and redirect to log in view if necessary

                        if (errorResponse != null) {

                            int status = errorResponse.getStatus();

                            if (status == 403) {

                                context.startActivity(new Intent(context, SignInActivity.class));

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
