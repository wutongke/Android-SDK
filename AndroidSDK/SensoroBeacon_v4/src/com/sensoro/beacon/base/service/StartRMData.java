package com.sensoro.beacon.base.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class StartRMData implements Parcelable {
	private RegionData regionData;
    private long scanPeriod;
    private long betweenScanPeriod;
	private String callbackPackageName;
	
    public StartRMData(RegionData regionData, String callbackPackageName) {
    	this.regionData = regionData;
    	this.callbackPackageName = callbackPackageName;
	}
    public StartRMData(long scanPeriod, long betweenScanPeriod) {
        this.scanPeriod = scanPeriod;
        this.betweenScanPeriod = betweenScanPeriod;
    }

    public StartRMData(RegionData regionData, String callbackPackageName, long scanPeriod, long betweenScanPeriod) {
        this.scanPeriod = scanPeriod;
        this.betweenScanPeriod = betweenScanPeriod;
        this.regionData = regionData;
        this.callbackPackageName = callbackPackageName;
    }


    public long getScanPeriod() { return scanPeriod; }
    public long getBetweenScanPeriod() { return betweenScanPeriod; }
    public RegionData getRegionData() {
    	return regionData;
    }
    public String getCallbackPackageName() {
    	return callbackPackageName;
    }
	public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(regionData, flags);
        out.writeString(callbackPackageName);
        out.writeLong(scanPeriod);
        out.writeLong(betweenScanPeriod);
    }

    public static final Parcelable.Creator<StartRMData> CREATOR
            = new Parcelable.Creator<StartRMData>() {
        public StartRMData createFromParcel(Parcel in) {
            return new StartRMData(in);
        }

        public StartRMData[] newArray(int size) {
            return new StartRMData[size];
        }
    };
    
    private StartRMData(Parcel in) { 
//    	Log.i("trs", this.getClass().getName());
    	regionData = in.readParcelable(this.getClass().getClassLoader());
        callbackPackageName = in.readString();
        scanPeriod = in.readLong();
        betweenScanPeriod = in.readLong();
    }

}
