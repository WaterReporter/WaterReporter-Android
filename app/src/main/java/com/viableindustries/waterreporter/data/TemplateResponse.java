package com.viableindustries.waterreporter.data;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Ryan Hamley on 10/20/14.
 * TemplateResponse is the response type of the API call getFields();
 * The hierarchy is TemplateResponse -> FieldResponse -> Field.
 */
public class TemplateResponse {
    @SerializedName("response")
    public FieldResponse fieldsObject;
}
