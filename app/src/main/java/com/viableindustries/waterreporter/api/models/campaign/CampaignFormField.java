package com.viableindustries.waterreporter.api.models.campaign;

import com.google.gson.annotations.SerializedName;

/**
 * Created by brendanmcintyre on 3/27/18.
 */

public class CampaignFormField {

    @SerializedName("instructions")
    public String instructions;

    @SerializedName("label")
    public String label;

    @SerializedName("name")
    public String name;

    @SerializedName("options")
    public String[] options;

    @SerializedName("type")
    public String type;

    @SerializedName("value")
    public Object value;

    public CampaignFormField(String aInstructions,
                             String aLabel,
                             String aName,
                             String[] aOptions,
                             String aType,
                             Object aValue) {

        this.instructions = aInstructions;
        this.label = aLabel;
        this.name = aName;
        this.options = aOptions;
        this.type = aType;
        this.value = aValue;

    }

}
