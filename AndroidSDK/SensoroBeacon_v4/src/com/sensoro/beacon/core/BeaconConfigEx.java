package com.sensoro.beacon.core;

/**
 * beaconConfig扩展类
 * 
 * @date 2014年4月19日
 */
class BeaconConfigEx {
	BeaconConfig beaconConfig; // BeaconConfig
	String beaconConfigJson; // BeaconConfig的json字符串
	long _date; // BeaconConfig的更新时间

	@Override
	public String toString() {
		return "BeaconConfigEx [beaconConfig=" + beaconConfig
				+ ", beaconConfigJson=" + beaconConfigJson + ", _date=" + _date
				+ "]";
	}

}
