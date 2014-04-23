package com.sensoro.beacon.core;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

class ActionBrief implements Parcelable {

	String type;
	HashMap<String, String> param;
	Event event;

	public static final Parcelable.Creator<ActionBrief> CREATOR = new Creator<ActionBrief>() {

		public ActionBrief createFromParcel(Parcel source) {

			ActionBrief event = new ActionBrief();
			event.type = source.readString();
			event.param = source.readHashMap(HashMap.class.getClassLoader());
			event.event = source.readParcelable(Event.class.getClassLoader());

			return event;
		}

		@Override
		public ActionBrief[] newArray(int size) {
			return new ActionBrief[size];
		}
	};

	public ActionBrief() {
		super();
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(Event event) {
		this.event = event;
	}

	public String getAction() {
		return type;
	}

	public void setAction(String action) {
		this.type = action;
	}

	public HashMap<String, String> getParam() {
		return param;
	}

	public void setParam(HashMap<String, String> param) {
		this.param = param;
	}

	@Override
	public String toString() {
		return "ActionBrief [action=" + type + ", param=" + param
				+ ", event=" + event + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type);
		dest.writeMap(param);
		dest.writeParcelable(event, 0);
	}
}
