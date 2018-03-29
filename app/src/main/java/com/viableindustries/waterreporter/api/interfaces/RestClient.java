package com.viableindustries.waterreporter.api.interfaces;

import com.viableindustries.waterreporter.api.interfaces.data.campaign.CampaignService;
import com.viableindustries.waterreporter.api.interfaces.data.comment.CommentService;
import com.viableindustries.waterreporter.api.interfaces.data.favorite.FavoriteService;
import com.viableindustries.waterreporter.api.interfaces.data.field_book.FieldBookService;
import com.viableindustries.waterreporter.api.interfaces.data.hashtag.HashTagService;
import com.viableindustries.waterreporter.api.interfaces.data.image.ImageService;
import com.viableindustries.waterreporter.api.interfaces.data.organization.OrganizationService;
import com.viableindustries.waterreporter.api.interfaces.data.post.ReportService;
import com.viableindustries.waterreporter.api.interfaces.data.snapshot.SnapshotService;
import com.viableindustries.waterreporter.api.interfaces.data.territory.HucGeometryService;
import com.viableindustries.waterreporter.api.interfaces.data.territory.TerritoryService;
import com.viableindustries.waterreporter.api.interfaces.data.trending.TrendingService;
import com.viableindustries.waterreporter.api.interfaces.data.user.UserService;
import com.viableindustries.waterreporter.api.interfaces.security.SecurityService;

import retrofit.RestAdapter;

/**
 * Created by brendanmcintyre on 9/12/17.
 */

public class RestClient {

    private static final String BASE_URL = "https://api.waterreporter.org/v2";

    private static final RestAdapter restAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(BASE_URL)
            .build();

    private static final UserService userService = restAdapter.create(UserService.class);
    private static final ReportService reportService = restAdapter.create(ReportService.class);
    private static final OrganizationService organizationService = restAdapter.create(OrganizationService.class);
    private static final FavoriteService favoriteService = restAdapter.create(FavoriteService.class);
    private static final ImageService imageService = restAdapter.create(ImageService.class);
    private static final HashTagService hashTagService = restAdapter.create(HashTagService.class);
    private static final CommentService commentService = restAdapter.create(CommentService.class);
    private static final TrendingService trendingService = restAdapter.create(TrendingService.class);
    private static final HucGeometryService hucGeometryService = restAdapter.create(HucGeometryService.class);
    private static final TerritoryService territoryService = restAdapter.create(TerritoryService.class);
    private static final CampaignService campaignService = restAdapter.create(CampaignService.class);
    private static final SnapshotService snapshotService = restAdapter.create(SnapshotService.class);
    private static final FieldBookService fieldBookService = restAdapter.create(FieldBookService.class);
    private static final SecurityService securityService = restAdapter.create(SecurityService.class);

    private RestClient() {
    }

    public static UserService getUserService() {
        return userService;
    }

    public static ReportService getReportService() {
        return reportService;
    }

    public static OrganizationService getOrganizationService() {
        return organizationService;
    }

    public static FavoriteService getFavoriteService() {
        return favoriteService;
    }

    public static ImageService getImageService() {
        return imageService;
    }

    public static HashTagService getHashTagService() {
        return hashTagService;
    }

    public static CommentService getCommentService() {
        return commentService;
    }

    public static TrendingService getTrendingService() {
        return trendingService;
    }

    public static HucGeometryService getHucGeometryService() {
        return hucGeometryService;
    }

    public static TerritoryService getTerritoryService() {
        return territoryService;
    }

    public static CampaignService getCampaignService() {
        return campaignService;
    }

    public static FieldBookService getFieldBookService() {
        return fieldBookService;
    }

    public static SnapshotService getSnapshotService() {
        return snapshotService;
    }

    public static SecurityService getSecurityService() {
        return securityService;
    }

}
