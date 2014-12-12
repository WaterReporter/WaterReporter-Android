package com.viableindustries.waterreporter.data;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

/**
 * Created by Ryan Hamley on 10/6/14.
 * This class defines the Retrofit interface with all the methods used to interact with the API
 * The top-level data classes returned from the API are:
 * CommonsCloudResponse, SingleFeatureResponse, TemplateResponse and PostResponse
 */
public interface CommonsCloudService {
    final String ENDPOINT = "https://api.commonscloud.org/v2";
    public static RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(ENDPOINT)
            .build();

    @GET("/type_2c1bd72acccf416aada3a6824731acc9.json")
    public void getReports(Callback<CommonsCloudResponse> response);

    @GET("/type_2c1bd72acccf416aada3a6824731acc9/{report}.json")
    public void getSingleReport(@Path("report") int reportId, Callback<SingleFeatureResponse> report);

    @Multipart
    @POST("/type_2c1bd72acccf416aada3a6824731acc9.json")
    public void postActivityReport(@Part("comments") String comment,
                                    @Part("useremail_address") String email,
                                    @Part("username") String name,
                                    @Part("usertitle") String title,
                                    @Part("date") String date,
                                    @Part("type_8f432efc18c545ea9578b4bdea860b4c") String type,
                                    @Part("type_0e9423a9a393481f82c4f22ff5954567") String activity,
                                    @Part("status") String status,
                                    @Part("geometry") String geometry,
                                    Callback<PostResponse> cb);

    @Multipart
    @POST("/type_2c1bd72acccf416aada3a6824731acc9.json")
    public void postActivityReportWithPhoto(@Part("comments") String comment,
                                             @Part("useremail_address") String email,
                                             @Part("username") String name,
                                             @Part("usertitle") String title,
                                             @Part("date") String date,
                                             @Part("type_8f432efc18c545ea9578b4bdea860b4c") String type,
                                             @Part("type_0e9423a9a393481f82c4f22ff5954567") String activity,
                                             @Part("status") String status,
                                             @Part("geometry") String geometry,
                                             @Part("attachment_76fc17d6574c401d9a20d18187f8083e") TypedFile photo,
                                             Callback<PostResponse> cb);

    @Multipart
    @POST("/type_2c1bd72acccf416aada3a6824731acc9.json")
    public void postPollutionReport(@Part("comments") String comment,
                                    @Part("useremail_address") String email,
                                    @Part("username") String name,
                                    @Part("usertitle") String title,
                                    @Part("date") String date,
                                    @Part("type_8f432efc18c545ea9578b4bdea860b4c") String type,
                                    @Part("type_05a300e835024771a51a6d3114e82abc") String pollution,
                                    @Part("status") String status,
                                    @Part("geometry") String geometry,
                                    Callback<PostResponse> cb);

    @Multipart
    @POST("/type_2c1bd72acccf416aada3a6824731acc9.json")
    public void postPollutionReportWithPhoto(@Part("comments") String comment,
                                    @Part("useremail_address") String email,
                                    @Part("username") String name,
                                    @Part("usertitle") String title,
                                    @Part("date") String date,
                                    @Part("type_8f432efc18c545ea9578b4bdea860b4c") String type,
                                    @Part("type_05a300e835024771a51a6d3114e82abc") String pollution,
                                    @Part("status") String status,
                                    @Part("geometry") String geometry,
                                    @Part("attachment_76fc17d6574c401d9a20d18187f8083e") TypedFile photo,
                                    Callback<PostResponse> cb);
}
