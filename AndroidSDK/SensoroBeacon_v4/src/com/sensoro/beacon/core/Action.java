package com.sensoro.beacon.core;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Description:	由beacon触发的回调类
 * 
 * @attribute HashMap<String, String> param:	交互参数,积分URL、发券URL等。开发者自行配置的信息,由开发者自行在SDK server中进行配置。
 * @attribute String action:	action的交互动作,有以下6种值:enter_spot(进入点),leave_spot(离开点),stay_spot(点停留),enter_zone(进入区),leave_zone(离开区),stay_zone(区停留)
 * @attribute {@link Zone} zone:	交互发生的动作 
 * @attribute {@link Spot} spot:	交互发生的点 
 */

public class Action implements Parcelable {

	HashMap<String, String> param;
	Spot spot;
	Zone zone;
	String action;

	public static final Creator<Action> CREATOR = new Creator<Action>() {

		public Action createFromParcel(Parcel source) {

			Action action = new Action();
			action.param = source.readHashMap(HashMap.class.getClassLoader());
			action.spot = source.readParcelable(SpotBrief.class
					.getClassLoader());
			action.zone = source.readParcelable(Zone.class.getClassLoader());
			action.action = source.readString();

			return action;
		}

		@Override
		public Action[] newArray(int size) {
			return new Action[size];
		}
	};

	public Action() {
		super();
	}

	public HashMap<String, String> getParam() {
		return param;
	}

	public void setParam(HashMap<String, String> param) {
		this.param = param;
	}

	public Spot getSpot() {
		return spot;
	}

	public void setSpot(Spot spot) {
		this.spot = spot;
	}

	public Zone getZone() {
		return zone;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeMap(param);
		dest.writeParcelable(spot, 0);
		dest.writeParcelable(zone, 0);
		dest.writeString(action);
	}

	@Override
	public String toString() {
		return "Action [param=" + param + ", spot=" + spot + ", zone=" + zone
				+ ", action=" + action + "]";
	}

}
