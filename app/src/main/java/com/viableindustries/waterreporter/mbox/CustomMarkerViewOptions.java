package com.viableindustries.waterreporter.mbox;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.mapboxsdk.annotations.BaseMarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by brendanmcintyre on 8/3/16.
 */

public class CustomMarkerViewOptions extends BaseMarkerViewOptions<CustomMarkerView, CustomMarkerViewOptions> {

    //private String abbrevName;
    //private int flagRes;
    private String imageUrl;

    public CustomMarkerViewOptions() {
    }

    protected CustomMarkerViewOptions(Parcel in) {
        position((LatLng) in.readParcelable(LatLng.class.getClassLoader()));
        snippet(in.readString());
        title(in.readString());
        flat(in.readByte() != 0);
        anchor(in.readFloat(), in.readFloat());
        selected = in.readByte() != 0;
        rotation(in.readFloat());
        if (in.readByte() != 0) {
            // this means we have an icon
            String iconId = in.readString();
            Bitmap iconBitmap = in.readParcelable(Bitmap.class.getClassLoader());
            Icon icon = IconFactory.recreate(iconId, iconBitmap);
            icon(icon);
        }
        imageUrl(in.readString());
        //flagRes(in.readInt());
    }

    @Override
    public CustomMarkerViewOptions getThis() {
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(getPosition(), flags);
        out.writeString(getSnippet());
        out.writeString(getTitle());
        out.writeByte((byte) (isFlat() ? 1 : 0));
        out.writeFloat(getAnchorU());
        out.writeFloat(getAnchorV());
        out.writeFloat(getInfoWindowAnchorU());
        out.writeFloat(getInfoWindowAnchorV());
        out.writeByte((byte) (selected ? 1 : 0));
        out.writeFloat(getRotation());
        Icon icon = getIcon();
        out.writeByte((byte) (icon != null ? 1 : 0));
        if (icon != null) {
            out.writeString(getIcon().getId());
            out.writeParcelable(getIcon().getBitmap(), flags);
        }
        out.writeString(imageUrl);
        //out.writeInt(flagRes);
    }

    @Override
    public CustomMarkerView getMarker() {
        return new CustomMarkerView(this, imageUrl);
    }

    public CustomMarkerViewOptions imageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return getThis();
    }

//    public CustomMarkerViewOptions flagRes(int flagRes) {
//        this.flagRes = flagRes;
//        return getThis();
//    }

    public static final Parcelable.Creator<CustomMarkerViewOptions> CREATOR
            = new Parcelable.Creator<CustomMarkerViewOptions>() {
        public CustomMarkerViewOptions createFromParcel(Parcel in) {
            return new CustomMarkerViewOptions(in);
        }

        public CustomMarkerViewOptions[] newArray(int size) {
            return new CustomMarkerViewOptions[size];
        }
    };

}
