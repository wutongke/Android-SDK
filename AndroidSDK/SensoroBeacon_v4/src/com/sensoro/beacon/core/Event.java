package com.sensoro.beacon.core;

import android.os.Parcel;
import android.os.Parcelable;

class Event implements Parcelable {

	String type;
	String name;
	String id; // spot id
	String zid; // zone id

	public static final Parcelable.Creator<Event> CREATOR = new Creator<Event>() {

		public Event createFromParcel(Parcel source) {

			Event event = new Event();
			event.type = source.readString();
			event.name = source.readString();
			event.id = source.readString();
			event.zid = source.readString();

			return event;
		}

		@Override
		public Event[] newArray(int size) {
			return new Event[size];
		}
	};

	public Event() {
		super();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getZid() {
		return zid;
	}

	public void setZid(String zid) {
		this.zid = zid;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type);
		dest.writeString(name);
		dest.writeString(id);
		dest.writeString(zid);
	}
}
