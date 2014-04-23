package com.sensoro.beacon.core;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 网络获取beaconConfig的info属性,该属性是一个简易的spot信息
 * 
 * @date 2014年4月15日
 */
class SpotBrief implements Parcelable {

	String name;// 名字
	String type;// beacon 的 type，如，店铺，广告牌，
	String address;// 地址：以 path 结构组织
	double lat;// 经度
	double lon;// 纬度
	long _date; // 更新时间

	public static final Creator<SpotBrief> CREATOR = new Creator<SpotBrief>() {

		@Override
		public SpotBrief createFromParcel(Parcel source) {

			SpotBrief brief = new SpotBrief();
			brief.name = source.readString();
			brief.type = source.readString();
			brief.address = source.readString();
			brief.lat = source.readDouble();
			brief.lon = source.readDouble();
			brief._date = source.readLong();

			return brief;
		}

		@Override
		public SpotBrief[] newArray(int size) {
			return null;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(type);
		dest.writeString(address);
		dest.writeDouble(lat);
		dest.writeDouble(lon);
		dest.writeLong(_date);
	}

	@Override
	public String toString() {
		return "SpotBrief [name=" + name + ", type=" + type + ", address="
				+ address + ", lat=" + lat + ", lon=" + lon + ", _date="
				+ _date + "]";
	}

}
