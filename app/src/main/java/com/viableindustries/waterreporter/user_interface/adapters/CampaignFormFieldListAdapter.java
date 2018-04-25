package com.viableindustries.waterreporter.user_interface.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
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

    private boolean authUserOwnsPost = true;

    private final List<CampaignFormField> sourceList;

    private SharedPreferences mFieldBookEntries;

    public CampaignFormFieldListAdapter(Context aContext, List<CampaignFormField> aFeatures) {

        super(aContext, 0, aFeatures);

        this.sourceList = aFeatures;

        this.mContext = aContext;

        this.mFieldBookEntries = mContext.getSharedPreferences(mContext.getString(R.string.field_book_entries_key), 0);

    }

    public CampaignFormFieldListAdapter(Context aContext, boolean aAuthUserOwnsPost, List<CampaignFormField> aFeatures) {

        super(aContext, 0, aFeatures);

        this.sourceList = aFeatures;

        this.mContext = aContext;

        this.authUserOwnsPost = aAuthUserOwnsPost;

        this.mFieldBookEntries = mContext.getSharedPreferences(mContext.getString(R.string.field_book_entries_key), 0);

    }

    private static class ViewHolder {
        Spinner formFieldSpinner;
        EditText formFieldValue;
        //        AutoCompleteTextView autoCompleteTextView;
        TextView formFieldLabel;
        TextView formFieldInstructions;
        LinearLayout campaignFormFieldItem;
    }

    private void setValue(final EditText editText, final CampaignFormField campaignFormField) {

        campaignFormField.value = editText.getText();

//        notifyDataSetChanged();

        mFieldBookEntries.edit().putString(campaignFormField.name, new Gson().toJson(campaignFormField)).apply();

    }

    private void setEnumValue(CampaignFormField campaignFormField, String selectedValue) {

        campaignFormField.value = selectedValue;

//        notifyDataSetChanged();

        mFieldBookEntries.edit().putString(campaignFormField.name, new Gson().toJson(campaignFormField)).apply();

    }

//    private CampaignFormField retrieveStoredEntry(String fieldName) {
//
//        String storedField = mFieldBookEntries.getString(fieldName, "");
//
//        CampaignFormField campaignFormField = new Gson().fromJson(storedField, CampaignFormField.class);
//
//        return campaignFormField;
//
//    }

    private String retrieveStoredEntry(String fieldName) {

        String storedField = mFieldBookEntries.getString(fieldName, "");

        return storedField;

    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        final CampaignFormFieldListAdapter.ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.campaign_form_field, parent, false);

            viewHolder = new CampaignFormFieldListAdapter.ViewHolder();

//            viewHolder.autoCompleteTextView = (AutoCompleteTextView) convertView.findViewById(R.id.autoCompleteTextView);
            viewHolder.formFieldSpinner = (Spinner) convertView.findViewById(R.id.formFieldSpinner);
            viewHolder.formFieldLabel = (TextView) convertView.findViewById(R.id.formFieldLabel);
            viewHolder.formFieldInstructions = (TextView) convertView.findViewById(R.id.formFieldInstructions);
            viewHolder.formFieldValue = (EditText) convertView.findViewById(R.id.formFieldValue);
            viewHolder.campaignFormFieldItem = (LinearLayout) convertView.findViewById(R.id.campaignFormFieldItem);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (CampaignFormFieldListAdapter.ViewHolder) convertView.getTag();

        }

        final CampaignFormField campaignFormField = sourceList.get(position);

        //
        // Populate layout elements
        //

        //
        // Reset input visibility
        //

        viewHolder.formFieldValue.setVisibility(View.VISIBLE);

        if (!authUserOwnsPost) {

            viewHolder.formFieldValue.setEnabled(false);
            viewHolder.formFieldValue.setInputType(InputType.TYPE_NULL);

        }

        viewHolder.formFieldSpinner.setVisibility(View.GONE);

        //
        // Reset AutoCompleteTextView adapter
        //

        viewHolder.formFieldSpinner.setAdapter(null);

        //
        // Set label text
        //

        viewHolder.formFieldLabel.setText(campaignFormField.label);

        if (campaignFormField.instructions != null &&
                campaignFormField.instructions.length() > 0) {

            viewHolder.formFieldInstructions.setVisibility(View.VISIBLE);

            viewHolder.formFieldInstructions.setText(campaignFormField.instructions);

        } else {

            viewHolder.formFieldInstructions.setVisibility(View.GONE);

        }

        //
        // Check for stored value
        //

        viewHolder.formFieldValue.setText("");

        final String storedValue = mFieldBookEntries.getString(campaignFormField.name, "");

        viewHolder.formFieldValue.setText(storedValue);

        //
        // Adjust input method type according to the field's data model
        //
        // See: https://developer.android.com/training/keyboard-input/style.html
        //

        switch (campaignFormField.type) {

            case "date":

                Log.v("FIELD_TYPE", "date");

                viewHolder.formFieldValue.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);

                break;

            case "enumeration":

                Log.v("FIELD_TYPE", "enumeration");

//                Log.v("FIELD_OPTIONS", campaignFormField.options.toString());
//
//                viewHolder.formFieldValue.setVisibility(View.GONE);

                viewHolder.formFieldSpinner.setVisibility(View.VISIBLE);

//                viewHolder.formFieldValue.setInputType(
//                        InputType.TYPE_CLASS_DATETIME|InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE|InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
//                );

                final ArrayAdapter<String> adapter =
                        new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, campaignFormField.options);

                if (!authUserOwnsPost) {

                    viewHolder.formFieldSpinner.setEnabled(false);
                    viewHolder.formFieldSpinner.setClickable(false);

                }

                viewHolder.formFieldSpinner.setAdapter(adapter);

                if (!storedValue.isEmpty()) {

                    viewHolder.formFieldSpinner.post(new Runnable() {
                        @Override
                        public void run() {
                            viewHolder.formFieldSpinner.setSelection(adapter.getPosition(storedValue));
                        }
                    });

                }

                break;

            case "float":

                Log.v("FIELD_TYPE", "float");

                viewHolder.formFieldValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                break;

            case "integer":

                Log.v("FIELD_TYPE", "integer");

                viewHolder.formFieldValue.setInputType(InputType.TYPE_CLASS_NUMBER);

                break;

            case "text":

                Log.v("FIELD_TYPE", "text");

                viewHolder.formFieldValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

                break;

            default:

                viewHolder.formFieldValue.setInputType(InputType.TYPE_CLASS_TEXT);

                break;

        }

        //
        // Listen for changes to the EditText and AutoCompleteTextView inputs and
        // tag the element with the result for later retrieval.
        //

        viewHolder.formFieldValue.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable s) {

//                viewHolder.formFieldLabel.setTag(viewHolder.formFieldValue.getText().toString());
//                campaignFormField.value = viewHolder.formFieldValue.getText();
//
//                notifyDataSetChanged();

//                setValue(viewHolder.formFieldValue, campaignFormField);

//                Log.v("FB-VAL", "storing value for " + campaignFormField.name);

                String inputText = viewHolder.formFieldValue.getText().toString();

                if (!inputText.isEmpty()) {

                    Log.v("FB-VAL", "storing value for " + campaignFormField.name + ": " + inputText);

                    mFieldBookEntries.edit().putString(campaignFormField.name, viewHolder.formFieldValue.getText().toString()).apply();

                }

            }

        });

        viewHolder.formFieldSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {

                String selection = parent.getItemAtPosition(pos).toString();

                viewHolder.formFieldValue.setText(selection);

//                setEnumValue(campaignFormField, selection);

                mFieldBookEntries.edit().putString(campaignFormField.name, selection).apply();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }

        });

//        viewHolder.autoCompleteTextView.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {}
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {}
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                viewHolder.formFieldLabel.setTag(viewHolder.autoCompleteTextView.getText().toString());
//            }
//
//        });

        return convertView;

    }

//    public void onItemSelected(AdapterView<?> parent, View view,
//                               int pos, long id) {
//        // An item was selected. You can retrieve the selected item using
//        // parent.getItemAtPosition(pos)
//
//        viewHolder.
//
//    }
//
//    public void onNothingSelected(AdapterView<?> parent) {
//        // Another interface callback
//    }

}
