package com.sensoro.beacon.core;

/**
 * beaconConfig��չ��
 * 
 * @date 2014��4��19��
 */
class BeaconConfigEx {
	BeaconConfig beaconConfig; // BeaconConfig
	String beaconConfigJson; // BeaconConfig��json�ַ���
	long _date; // BeaconConfig�ĸ���ʱ��

	@Override
	public String toString() {
		return "BeaconConfigEx [beaconConfig=" + beaconConfig
				+ ", beaconConfigJson=" + beaconConfigJson + ", _date=" + _date
				+ "]";
	}

}
