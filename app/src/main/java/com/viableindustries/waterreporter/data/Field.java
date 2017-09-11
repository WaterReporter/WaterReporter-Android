package com.viableindustries.waterreporter.data;

/**
 * Created by Ryan Hamley on 10/20/14.
 * This class defines the fields in the list returned from the API when calling getFields().
 */
class Field {
    public String data_type;
    public String help;
    public int id;
    public boolean is_listed;
    public boolean is_public;
    public boolean is_required;
    public boolean is_searchable;
    public boolean is_visible;
    private final String label;
    public String name;
    private final String options;
    public String relationship;
    public boolean status;
    public int weight;

    public String[] getOptions() {
        return options.trim().split(",");
    }

    public String getLabel() {
        return label;
    }
}
