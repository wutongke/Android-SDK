package com.sensoro.beacon.core;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * �����ȡbeaconConfig��info����,��������һ�����׵�spot��Ϣ
 * 
 * @date 2014��4��15��
 */
class SpotBrief implements Parcelable {

	String name;// ����
	String type;// beacon �� type���磬���̣�����ƣ�
	String address;// ��ַ���� path �ṹ��֯
	double lat;// ����
	double lon;// γ��
	long _date; // ����ʱ��

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
