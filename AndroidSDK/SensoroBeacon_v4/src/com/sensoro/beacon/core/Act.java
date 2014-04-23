package com.sensoro.beacon.core;

import android.os.Parcel;
import android.os.Parcelable;

class Act implements Parcelable {

	ActEvent enter;
	ActEvent leave;
	ActEvent stay;

	public static final Creator<Act> CREATOR = new Creator<Act>() {

		@Override
		public Act createFromParcel(Parcel source) {

			Act act = new Act();

			act.enter = source.readParcelable(ActEvent.class.getClassLoader());
			act.leave = source.readParcelable(ActEvent.class.getClassLoader());
			act.stay = source.readParcelable(ActEvent.class.getClassLoader());

			return act;
		}

		@Override
		public Act[] newArray(int size) {
			return null;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(enter, 0);
		dest.writeParcelable(leave, 0);
		dest.writeParcelable(stay, 0);
	}

	@Override
	public String toString() {
		return "Act [enter=" + enter + ", leave=" + leave + ", stay=" + stay
				+ "]";
	}

}
