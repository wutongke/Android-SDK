package com.sensoro.beacon.core;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

class ZoneConfig implements Parcelable {

	HashMap<String, String> param;
	String id;
	Act act;
	long _date; // sdk内部使用的更新时间

	public Act getAct() {
		return act;
	}

	public void setAct(Act act) {
		this.act = act;
	}

	public HashMap<String, String> getParam() {
		return param;
	}

	public void setParam(HashMap<String, String> param) {
		this.param = param;
	}

	public static final Creator<ZoneConfig> CREATOR = new Creator<ZoneConfig>() {

		@Override
		public ZoneConfig createFromParcel(Parcel source) {

			ZoneConfig zoneConfig = new ZoneConfig();

			zoneConfig.id = source.readString();
			zoneConfig.param = source.readHashMap(HashMap.class
					.getClassLoader());
			zoneConfig.act = source.readParcelable(Act.class.getClassLoader());

			return zoneConfig;
		}

		@Override
		public ZoneConfig[] newArray(int size) {
			return null;
		}
	};

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (this == o) {
			return true;
		}
		if (!(o instanceof ZoneConfig)) {
			return false;
		}
		ZoneConfig config = (ZoneConfig) o;
		if (this.id.equals(config.id)) {
			return true;
		}

		return false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeMap(param);
		dest.writeParcelable(act, 0);
	}

	@Override
	public String toString() {
		return "ZoneConfig [param=" + param + ", id=" + id + ", act=" + act
				+ ", _date=" + _date + "]";
	}

}
