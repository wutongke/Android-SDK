package com.sensoro.beacon.core;

import android.os.Parcel;
import android.os.Parcelable;

class BeaconSet implements Parcelable {

	long spotTimer; // spot stay触发的最小时间单位(毫秒)
	long zoneTimer; // zone stay触发的最小时间单位(毫秒)
	long offlineTimer; // 获取离线数据的最小时间间隔(毫秒)
	long offlineDist; // 获取离线数据的触发距离

	public BeaconSet() {
		super();
	}

	public static final Creator<BeaconSet> CREATOR = new Creator<BeaconSet>() {

		@Override
		public BeaconSet createFromParcel(Parcel source) {

			BeaconSet beaconSet = new BeaconSet();

			beaconSet.spotTimer = source.readLong();
			beaconSet.zoneTimer = source.readLong();
			beaconSet.offlineTimer = source.readLong();
			beaconSet.offlineDist = source.readLong();

			return beaconSet;
		}

		@Override
		public BeaconSet[] newArray(int size) {
			return null;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeLong(spotTimer);
		dest.writeLong(zoneTimer);
		dest.writeLong(offlineTimer);
		dest.writeLong(offlineDist);
	}

}
