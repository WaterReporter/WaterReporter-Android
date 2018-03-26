package com.viableindustries.waterreporter.api.interfaces.data.snapshot;

import com.viableindustries.waterreporter.api.models.favorite.Favorite;
import com.viableindustries.waterreporter.api.models.favorite.FavoritePostBody;
import com.viableindustries.waterreporter.api.models.organization.Organization;
import com.viableindustries.waterreporter.api.models.snapshot.CampaignLeaderboard;
import com.viableindustries.waterreporter.api.models.snapshot.CampaignSnapshot;
import com.viableindustries.waterreporter.api.models.snapshot.OrganizationSnapshot;
import com.viableindustries.waterreporter.api.models.snapshot.UserSnapshot;

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

    @GET("/data/snapshot/campaign/{campaignId}/leaderboard")
    void getCampaignLeaderboard(@Header("Authorization") String authorization,
                     @Header("Content-Type") String contentType,
                     @Path("campaignId") int campaignId,
                     Callback<CampaignLeaderboard> campaignLeaderboardCallback);

    @GET("/data/snapshot/campaign/{campaignId}/users")
    void getCampaignUsers(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("campaignId") int campaignId,
                                Callback<CampaignLeaderboard> campaignLeaderboardCallback);

    @GET("/data/snapshot/campaign/{campaignId}/groups")
    void getCampaignGroups(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("campaignId") int campaignId,
                                Callback<CampaignLeaderboard> campaignLeaderboardCallback);

    @GET("/data/snapshot/campaign/{campaignId}/watersheds")
    void getCampaignWatersheds(@Header("Authorization") String authorization,
                                @Header("Content-Type") String contentType,
                                @Path("campaignId") int campaignId,
                                Callback<CampaignLeaderboard> campaignLeaderboardCallback);

    @GET("/data/snapshot/organization/{organizationId}")
    void getOrganization(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Path("organizationId") int organizationId,
                 Callback<OrganizationSnapshot> organizationSnapshotCallback);

    @GET("/data/snapshot/user/{userId}")
    void getUser(@Header("Authorization") String authorization,
                 @Header("Content-Type") String contentType,
                 @Path("userId") int userId,
                 Callback<UserSnapshot> userSnapshotCallback);

}
