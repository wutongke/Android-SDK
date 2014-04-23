package com.sensoro.beacon.base;

public class WifiBeacon {
	private String uuid;
	private int major;
	private int minor;

	@Override
	public String toString() {
		return "Beacon{" + "uuid='" + uuid + '\'' + ", major=" + major
				+ ", minor=" + minor + '}';
	}

	public String getUuid() {
		return uuid;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public WifiBeacon(String uuid, int major, int minor) {
		this.uuid = uuid;
		this.major = major;
		this.minor = minor;
	}
}
