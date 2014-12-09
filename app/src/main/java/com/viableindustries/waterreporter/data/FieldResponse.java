package com.viableindustries.waterreporter.data;

import java.util.List;

/**
 * Created by Ryan Hamley on 10/20/14.
 * This class is a list of fields returned by the getFields() API call.
 */
public class FieldResponse {
    List<Field> fields;

    public List<Field> getFields(){
        return fields;
    }
}
