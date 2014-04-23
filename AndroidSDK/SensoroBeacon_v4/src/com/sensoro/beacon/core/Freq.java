package com.sensoro.beacon.core;

import android.os.Parcel;
import android.os.Parcelable;

class Freq implements Parcelable {

	int daily;
	long interval;

	public static final Creator<Freq> CREATOR = new Creator<Freq>() {

		@Override
		public Freq createFromParcel(Parcel source) {

			Freq freq = new Freq();
			freq.daily = source.readInt();
			freq.interval = source.readLong();

			return freq;
		}

		@Override
		public Freq[] newArray(int size) {
			return null;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(daily);
		dest.writeLong(interval);
	}

	@Override
	public String toString() {
		return "Freq [daily=" + daily + ", interval=" + interval + "]";
	}

}
