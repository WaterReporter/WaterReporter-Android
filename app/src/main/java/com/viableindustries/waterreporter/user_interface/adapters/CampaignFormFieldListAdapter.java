package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.api.models.campaign.CampaignFormField;

import java.util.List;

/**
 * Created by brendanmcintyre on 3/29/18.
 */

public class CampaignFormFieldListAdapter extends ArrayAdapter<CampaignFormField> {

    private final Context mContext;

    protected String name;

    protected int id;

    private final List<CampaignFormField> sourceList;

    public CampaignFormFieldListAdapter(Context aContext, List<CampaignFormField> features) {

        super(aContext, 0, features);

        this.sourceList = features;

        this.mContext = aContext;

    }

    private static class ViewHolder {
        EditText formFieldValue;
        TextView formFieldLabel;
        TextView formFieldInstructions;
        LinearLayout campaignFormField;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        CampaignFormFieldListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.campaign_form_field, parent, false);

            viewHolder = new CampaignFormFieldListAdapter.ViewHolder();

            viewHolder.formFieldLabel = (TextView) convertView.findViewById(R.id.formFieldLabel);
            viewHolder.formFieldInstructions = (TextView) convertView.findViewById(R.id.formFieldInstructions);
            viewHolder.formFieldValue = (EditText) convertView.findViewById(R.id.formFieldValue);
            viewHolder.campaignFormField = (LinearLayout) convertView.findViewById(R.id.campaignFormField);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (CampaignFormFieldListAdapter.ViewHolder) convertView.getTag();

        }

        final CampaignFormField campaignFormField = sourceList.get(position);

        // Populate layout elements

        viewHolder.formFieldLabel.setText(campaignFormField.label);

        if (campaignFormField.instructions != null &&
                campaignFormField.instructions.length() > 0) {

            viewHolder.formFieldInstructions.setVisibility(View.VISIBLE);

            viewHolder.formFieldInstructions.setText(campaignFormField.instructions);

        } else {

            viewHolder.formFieldInstructions.setVisibility(View.GONE);

        }

        if (campaignFormField.value != null &&
                campaignFormField.value.toString().length() > 0) {

            viewHolder.formFieldValue.setText(campaignFormField.value.toString());

        }

        return convertView;

    }

}
