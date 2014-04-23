package com.sensoro.beacon.core;

class ConfigDBUtils {
	DataBaseAdpter dataBaseAdpter;

	public ConfigDBUtils(DataBaseAdpter dataBaseAdpter) {
		super();
		this.dataBaseAdpter = dataBaseAdpter;
	}

	/**
	 * 从数据库读取BeaconConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年4月16日
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
	 * 从数据库读取ZoneConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年4月16日
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
