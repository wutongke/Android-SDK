package com.sensoro.beacon.core;

class ZoneConfigEx {
	ZoneConfig zoneConfig;
	String zoneConfigJson;
	long _date; // ZoneConfig的更新时间

	@Override
	public String toString() {
		return "ZoneConfigEx [zoneConfig=" + zoneConfig + ", zoneConfigJson="
				+ zoneConfigJson + ", _date=" + _date + "]";
	}

}
