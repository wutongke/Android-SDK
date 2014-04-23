package com.sensoro.beacon.base.service;

import com.sensoro.beacon.base.Region;

import android.os.Parcel;
import android.os.Parcelable;

public class RegionData extends Region implements Parcelable {
    public RegionData(String uniqueId, String proximityUuid, Integer major,
			Integer minor) {
		super(uniqueId, proximityUuid, major, minor);
	}
    public RegionData(Region region) {
    	super(region);
    }

	public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(major == null ? -1 : major);
        out.writeInt(minor == null ? -1 : minor);
        out.writeString(proximityUuid);
        out.writeString(uniqueId);
    }

    public static final Parcelable.Creator<RegionData> CREATOR
            = new Parcelable.Creator<RegionData>() {
        public RegionData createFromParcel(Parcel in) {
            return new RegionData(in);
        }

        public RegionData[] newArray(int size) {
            return new RegionData[size];
        }
    };
    
    private RegionData(Parcel in) { 
	   	 major = in.readInt();
	   	 if (major == -1) {
	   		 major = null;
	   	 }
	   	 minor = in.readInt();
	   	 if (minor == -1) {
	   		 minor = null;
	   	 }
	   	 proximityUuid = in.readString();
	   	 uniqueId = in.readString();
    }

}
