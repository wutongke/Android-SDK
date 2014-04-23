package com.sensoro.beacon.core;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Description:	��beacon�����������ɶ���㹹�ɡ�
 * 
 * @attribute String id:	���������ж�������� id.
 * @attribute HashMap<String,String> param:	�����������������������õ���Ϣ���ɿ�����������SDK server�н�������.
 */

public class Zone implements Parcelable {

	String zid;
	HashMap<String, String> param;

	public HashMap<String, String> getParam() {
		return param;
	}

	public void setParam(HashMap<String, String> param) {
		this.param = param;
	}

	public static final Creator<Zone> CREATOR = new Creator<Zone>() {

		@Override
		public Zone createFromParcel(Parcel source) {
			Zone zone = new Zone();
			zone.zid = source.readString();
			zone.param = source.readHashMap(HashMap.class.getClassLoader());

			return zone;
		}

		@Override
		public Zone[] newArray(int size) {
			return null;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(zid);
		dest.writeMap(param);
	}

	public String getZid() {
		return zid;
	}

	public void setZid(String zid) {
		this.zid = zid;
	}

	@Override
	public String toString() {
		return "Zone [zid=" + zid + ", param=" + param + "]";
	}

}
