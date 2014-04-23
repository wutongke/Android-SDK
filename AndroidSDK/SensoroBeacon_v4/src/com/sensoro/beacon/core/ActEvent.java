package com.sensoro.beacon.core;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

class ActEvent implements Parcelable {
	boolean remote; // 表示action是否需要网络访问
	Freq freq;
	String type;
	HashMap<String, String> param;

	public static final Creator<ActEvent> CREATOR = new Creator<ActEvent>() {

		@Override
		public ActEvent createFromParcel(Parcel source) {

			ActEvent actEvent = new ActEvent();
			int tmp = source.readInt();
			if (tmp == 1) {
				actEvent.remote = true;
			} else if (tmp == 0) {
				actEvent.remote = false;
			}
			actEvent.freq = source.readParcelable(Freq.class.getClassLoader());
			actEvent.param = source
					.readHashMap(HashMap.class.getClassLoader());

			return actEvent;
		}

		@Override
		public ActEvent[] newArray(int size) {
			return null;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (remote) {
			dest.writeInt(1);
		} else {
			dest.writeInt(0);
		}
		dest.writeParcelable(freq, 0);
		dest.writeString(type);
		dest.writeMap(param);
	}

	@Override
	public String toString() {
		return "ActEvent [remote=" + remote + ", freq=" + freq + ", type="
				+ type + ", param=" + param + "]";
	}

}
