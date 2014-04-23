package com.sensoro.beacon.core;

import java.util.ArrayList;
import java.util.HashMap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * �������ȡ��������Ϣ
 * 
 * @date 2014��1��21��
 */
class BeaconConfig implements Parcelable {

	String _id; // sdk�ڲ�ʹ��
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
	 * ����equal,��beacon idһ��ʱ���
	 * 
	 * @param
	 * @return
	 * @date 2014��2��10��
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
