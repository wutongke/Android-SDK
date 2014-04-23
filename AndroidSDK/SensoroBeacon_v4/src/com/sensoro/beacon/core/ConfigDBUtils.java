package com.sensoro.beacon.core;

class ConfigDBUtils {
	DataBaseAdpter dataBaseAdpter;

	public ConfigDBUtils(DataBaseAdpter dataBaseAdpter) {
		super();
		this.dataBaseAdpter = dataBaseAdpter;
	}

	/**
	 * �����ݿ��ȡBeaconConfig
	 * 
	 * @param
	 * @return
	 * @date 2014��4��16��
	 * @author trs
	 */
	public BeaconConfig getBeaconConfig(BleEvent bleEvent)
			throws SensoroException {

		if (dataBaseAdpter == null) {

			return null;
		}
		String info = dataBaseAdpter.getBeaconConfigInfo(bleEvent.id);
		BeaconConfig beaconConfig = BeaconJsonUtils.parseBeaconConfig(info);

		return beaconConfig;
	}

	/**
	 * �����ݿ��ȡZoneConfig
	 * 
	 * @param
	 * @return
	 * @date 2014��4��16��
	 * @author trs
	 */
	public ZoneConfig getZoneConfig(String zid) throws SensoroException {

		if (dataBaseAdpter == null) {

			return null;
		}
		String info = dataBaseAdpter.getZoneConfigInfo(zid);
		ZoneConfig zoneConfig = BeaconJsonUtils.parseZoneConfig(info);

		return zoneConfig;
	}

}
