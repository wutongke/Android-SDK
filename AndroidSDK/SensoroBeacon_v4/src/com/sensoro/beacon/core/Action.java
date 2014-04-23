package com.sensoro.beacon.core;

import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Description:	��beacon�����Ļص���
 * 
 * @attribute HashMap<String, String> param:	��������,����URL����ȯURL�ȡ��������������õ���Ϣ,�ɿ�����������SDK server�н������á�
 * @attribute String action:	action�Ľ�������,������6��ֵ:enter_spot(�����),leave_spot(�뿪��),stay_spot(��ͣ��),enter_zone(������),leave_zone(�뿪��),stay_zone(��ͣ��)
 * @attribute {@link Zone} zone:	���������Ķ��� 
 * @attribute {@link Spot} spot:	���������ĵ� 
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
