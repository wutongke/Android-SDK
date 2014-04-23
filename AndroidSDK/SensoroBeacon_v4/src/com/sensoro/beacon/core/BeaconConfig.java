package com.sensoro.beacon.core;

import java.util.ArrayList;
import java.util.HashMap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 从网络获取的配置信息
 * 
 * @date 2014年1月21日
 */
class BeaconConfig implements Parcelable {

	String _id; // sdk内部使用
	SpotBrief info;
	Act act;
	HashMap<String, String> param;
	String id;
	ArrayList<String> zids;

	public BeaconConfig() {
		super();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(_id);
		dest.writeParcelable(info, 0);
		dest.writeParcelable(act, 0);
		dest.writeMap(param);
		dest.writeString(id);
		dest.writeList(zids);

	}

	public static final Creator<BeaconConfig> CREATOR = new Creator<BeaconConfig>() {

		@Override
		public BeaconConfig createFromParcel(Parcel source) {

			BeaconConfig beaconConfig = new BeaconConfig();

			beaconConfig._id = source.readString();
			beaconConfig.info = source.readParcelable(SpotBrief.class
					.getClassLoader());
			beaconConfig.act = source
					.readParcelable(Act.class.getClassLoader());
			beaconConfig.param = source.readHashMap(HashMap.class
					.getClassLoader());
			beaconConfig.id = source.readString();
			source.readList(beaconConfig.zids, String.class.getClassLoader());

			return beaconConfig;
		}

		@Override
		public BeaconConfig[] newArray(int size) {
			return null;
		}
	};

	/**
	 * 重载equal,当beacon id一致时相等
	 * 
	 * @param
	 * @return
	 * @date 2014年2月10日
	 * @author trs
	 */
	@Override
	public boolean equals(Object o) {

		if (o == null) {
			return false;
		}
		if (this == o) {
			return true;
		}
		if (!(o instanceof BeaconConfig)) {
			return false;
		}
		BeaconConfig config = (BeaconConfig) o;
		if (this.id.equals(config._id)) {
			return true;
		}

		return false;
	}

	@Override
	public String toString() {
		return "BeaconConfig [_id=" + _id + ", info=" + info + ", act=" + act
				+ ", param=" + param + ", id=" + id + ", zids=" + zids + "]";
	}

}
