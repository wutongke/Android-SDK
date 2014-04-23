package com.sensoro.beacon.core;

import android.os.Parcel;
import android.os.Parcelable;

import com.sensoro.beacon.base.IBeacon;
import com.sensoro.beacon.base.WifiBeacon;

/**
 * BLE层发生的事件,从HW得来
 * 
 * @date 2014年1月21日
 */
class BleEvent implements Parcelable{
	
	private static final String MINUS = "-";
	
	long time; // 时间
	String uuid; // beacon id
	int major; // beacon major
	int minor; // beacon id
	String id; //以一定的规则组成的id
	BeaconConfig beaconConfig;// BeaconConfig

	public static final Creator<BleEvent> CREATOR = new Creator<BleEvent>() {

		@Override
		public BleEvent createFromParcel(Parcel source) {
			BleEvent bleEvent = new BleEvent();
			bleEvent.time = source.readLong();
			bleEvent.uuid = source.readString();
			bleEvent.major = source.readInt();
			bleEvent.minor = source.readInt();
			bleEvent.id = source.readString();
			bleEvent.beaconConfig = source.readParcelable(BeaconConfig.class.getClassLoader());
			
			return bleEvent;
		}

		@Override
		public BleEvent[] newArray(int size) {
			return null;
		}
	};
	public BleEvent(IBeacon iBeacon, long time) {
		super();
		this.time = time;
		//转换成大写
		this.uuid = iBeacon.getProximityUuid().toUpperCase();
		this.major = iBeacon.getMajor();
		this.minor = iBeacon.getMinor();
		id = GeneratreID(uuid, major, minor).toString();
	}

	public BleEvent(WifiBeacon wifiBeacon, long time) {
		super();
		this.time = time;
		//转换成大写
		this.uuid = wifiBeacon.getUuid();
		this.major = wifiBeacon.getMajor();
		this.minor = wifiBeacon.getMinor();
		id = GeneratreID(uuid, major, minor).toString();
	}

	public BleEvent(long time, String uuid, int major, int minor,
			BeaconConfig beaconConfig) {
		super();
		this.time = time;
		this.uuid = uuid.toUpperCase();
		this.major = major;
		this.minor = minor;
		id = GeneratreID(uuid, major, minor).toString();
		this.beaconConfig = beaconConfig;
	}
	
	
	
	public BleEvent() {
		super();
	}

	public BleEvent(long time, String uuid, int major, int minor, String id,
			BeaconConfig beaconConfig) {
		super();
		this.time = time;
		this.uuid = uuid;
		this.major = major;
		this.minor = minor;
		this.id = id;
		this.beaconConfig = beaconConfig;
	}

	public static StringBuffer GeneratreID(String uuid, int major, int minor) {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(uuid);
		buffer.append(MINUS);
		buffer.append(String.format("%04X", major));
		buffer.append(MINUS);
		buffer.append(String.format("%04X", minor));
		
		return buffer;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(time);
		dest.writeString(uuid);
		dest.writeInt(major);
		dest.writeInt(minor);
		dest.writeString(id);
		dest.writeParcelable(beaconConfig, 0);
	}
	
	/**
	 * 重载equal,当beacon id一致时相等
	 * @param
	 * @return
	 * @date 2014年2月10日
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
		if (!(o instanceof BleEvent)) {
			return false;
		}
		BleEvent bleEvent = (BleEvent) o;
		if (this.id.equals(bleEvent.id)) {
			return true;
		}
		
		return false;
	}
	public static Beacon toBeacon(BleEvent bleEvent) {
		if (bleEvent == null) {
			return null;
		}
		
		Beacon beacon = new Beacon();
		beacon.uuid = bleEvent.uuid;
		beacon.major = bleEvent.major +"";
		beacon.minor = bleEvent.minor +"";
		
		return beacon;
	}
}
