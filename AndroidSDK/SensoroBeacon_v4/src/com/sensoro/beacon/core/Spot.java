package com.sensoro.beacon.core;

import java.util.Arrays;
import java.util.HashMap;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Description: һ�� beacon�Ͷ�Ӧ��һ����.
 * 
 * @attribute HashMap<String, String> param:
 *            ��������,����URL����ȯURL�ȡ��������������õ���Ϣ,�ɿ�����������SDK server�н������á�
 * @attribute String name: �������.
 * @attribute String type: beacon������.����,���̡�����Ƶ�.
 * @attribute String address: beacon�ĵ�ַ·��.
 * @attribute double lat: beacon����λ�õľ���.
 * @attribute double lon: beacon����λ�õ�γ��.
 * @attribute String id: ���id.�������������õ���Ϣ,�ɿ�����������SDK server�н������á�
 * @attribute String[] zids: ������������(һ����������ڶ������).�������������õ���Ϣ,�ɿ�����������SDK
 *            server�н������á�
 * @attribute HashMap<String,String> param: �����������������������õ���Ϣ���ɿ�����������SDK
 *            server�н�������.
 */
public class Spot implements Parcelable {

	// ��������ÿ����ͬ�� app ����ͬ
	// String _id;// ����ڲ� id
	// String _location;// ��װλ�ã��磺�ſڣ���̨��
	// String _owner;// ��װ��
	// Date _date;// ��װʱ��

	String name;// ����
	String type;// beacon �� type���磬���̣�����ƣ�
	String address;// ��ַ���� path �ṹ��֯
	double lat;// ����
	double lon;// γ��
	long _date; // ����ʱ��

	// -- ��չ��ÿ�� app �ɲ�ͬ
	String id; // ���������ж���� id���� sdk Ҫ�ã���˵������
	String[] zids; // ������� APP ������ʲô��,�������Լ����������id������
	HashMap<String, String> param; // �û��Զ������

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
