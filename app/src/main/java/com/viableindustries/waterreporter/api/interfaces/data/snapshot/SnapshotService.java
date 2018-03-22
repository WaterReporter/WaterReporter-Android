package com.viableindustries.waterreporter.api.interfaces.data.snapshot;

import com.viableindustries.waterreporter.api.models.favorite.Favorite;
import com.viableindustries.waterreporter.api.models.favorite.FavoritePostBody;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.snapshot.CampaignSnapshot;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by brendanmcintyre on 3/21/18.
 */

public interface SnapshotService {

    @GET("/data/snapshot/campaign/{campaignId}")
    void getCampaign(@Header("Authorization") String authorization,
                         @Header("Content-Type") String contentType,
                         @Path("campaignId") int campaignId,
                         Callback<CampaignSnapshot> campaignSnapshotCallback);

    @GET("/data/snapshot/organization/{organizationId}")
    void getOrganization(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Path("organizationId") int organizationId,
                 Callback<Organization> organizationCallback);

    @GET("/data/snapshot/user/{userId}")
    void getUser(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Path("userId") int userId,
                 Callback<Organization> organizationCallback);

}
