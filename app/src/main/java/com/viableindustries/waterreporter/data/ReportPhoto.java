package com.viableindustries.waterreporter.data;

/**
 * Created by Ryan Hamley on 10/9/14.
 * ReportPhoto is the photo information returned with a report by the API.
 */
public class ReportPhoto {
    public String caption;
    public String created;
    public String credit;
    public String credit_link;
    public String filename;
    public String filepath;
    public int id;
    public String status;

    public String getFilepath(){
        return "http://" + filepath;
    }
}
