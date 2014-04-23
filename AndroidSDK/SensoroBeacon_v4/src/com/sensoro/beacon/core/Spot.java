package com.sensoro.beacon.core;

import java.util.Arrays;
import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Description: 一个 beacon就对应着一个点.
 * 
 * @attribute HashMap<String, String> param:
 *            交互参数,积分URL、发券URL等。开发者自行配置的信息,由开发者自行在SDK server中进行配置。
 * @attribute String name: 点的名称.
 * @attribute String type: beacon的类型.比如,店铺、广告牌等.
 * @attribute String address: beacon的地址路径.
 * @attribute double lat: beacon所在位置的经度.
 * @attribute double lon: beacon所在位置的纬度.
 * @attribute String id: 点的id.开发者自行配置的信息,由开发者自行在SDK server中进行配置。
 * @attribute String[] zids: 点所属的区域(一个点可能属于多个区域).开发者自行配置的信息,由开发者自行在SDK
 *            server中进行配置。
 * @attribute HashMap<String,String> param: 交互参数。开发者自行配置的信息，由开发者自行在SDK
 *            server中进行配置.
 */
public class Spot implements Parcelable {

	// 基础，对每个不同的 app 都相同
	// String _id;// 点的内部 id
	// String _location;// 安装位置（如：门口，款台）
	// String _owner;// 安装者
	// Date _date;// 安装时间

	String name;// 名字
	String type;// beacon 的 type，如，店铺，广告牌，
	String address;// 地址：以 path 结构组织
	double lat;// 经度
	double lon;// 纬度
	long _date; // 更新时间

	// -- 扩展，每个 app 可不同
	String id; // 开发者自行定义的 id，但 sdk 要用，因此单独提出
	String[] zids; // 点在这个 APP 里属于什么区,开发者自己定义的区域id的数组
	HashMap<String, String> param; // 用户自定义参数

	public static final Creator<Spot> CREATOR = new Creator<Spot>() {

		@Override
		public Spot createFromParcel(Parcel source) {

			Spot spot = new Spot();
			spot.name = source.readString();
			spot.lat = source.readDouble();
			spot.lon = source.readDouble();
			spot.type = source.readString();
			spot.address = source.readString();
			spot.param = source.readHashMap(HashMap.class.getClassLoader());
			spot.id = source.readString();
			spot.zids = source.createStringArray();
			spot._date = source.readLong();

			return spot;
		}

		@Override
		public Spot[] newArray(int size) {
			return null;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeDouble(lat);
		dest.writeDouble(lon);
		dest.writeString(type);
		dest.writeString(address);
		dest.writeMap(param);
		dest.writeString(id);
		dest.writeStringArray(zids);
		dest.writeLong(_date);
	}

	@Override
	public String toString() {
		return "Spot [name=" + name + ", type=" + type + ", address=" + address
				+ ", lat=" + lat + ", lon=" + lon + ", _date=" + _date
				+ ", id=" + id + ", zids=" + Arrays.toString(zids) + ", param="
				+ param + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String[] getZids() {
		return zids;
	}

	public void setZids(String[] zids) {
		this.zids = zids;
	}

	public HashMap<String, String> getParam() {
		return param;
	}

	public void setParam(HashMap<String, String> param) {
		this.param = param;
	}

	public static Creator<Spot> getCreator() {
		return CREATOR;
	}
}
