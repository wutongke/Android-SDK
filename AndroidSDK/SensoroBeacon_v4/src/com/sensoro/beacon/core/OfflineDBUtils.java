package com.sensoro.beacon.core;

public class OfflineDBUtils {
	DataBaseAdpter dataBaseAdpter;

	public OfflineDBUtils(DataBaseAdpter dataBaseAdpter) {
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
		BeaconConfig beaconConfig = BeaconJsonUtils.parseBeaconConfigInfo(info);

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
		ZoneConfig zoneConfig = BeaconJsonUtils.parseZoneConfigInfo(info);

		return zoneConfig;
	}

	/**
	 * 从数据库读取ActionRecord
	 * 
	 * @param
	 * @return
	 * @date 2014年4月17日
	 * @author zwz
	 */
	public ActionRecord getActionRecord(String id, int type, int name)
			throws SensoroException {
		if (dataBaseAdpter == null) {
			return null;
		}
		if (id == null) {
			return null;
		}
		return dataBaseAdpter.getActionRecord(id, type, name);
	}

	/**
	 * 添加ActionRecord记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月17日
	 * @author zwz
	 */
	public boolean addActionRecord(String id, int type, int name,
			long timestamp, int times) throws SensoroException {
		if (dataBaseAdpter == null) {
			return false;
		}
		return dataBaseAdpter.addActionRecord(id, type, name, timestamp, times);
	}

	/**
	 * 更新ActionRecord记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月17日
	 * @author zwz
	 */
	public int updateActionRecord(String id, int type, int name,
			long timestamp, int times) throws SensoroException {
		if (dataBaseAdpter == null) {
			return -1;
		}
		return dataBaseAdpter.updateActionRecord(id, type, name, timestamp,
				times);
	}

	/**
	 * 添加BeaconConfig到数据库
	 * 
	 * @param
	 * @return
	 * @date 2014年4月19日
	 * @author trs
	 */
	public boolean addBeaconConfig(String id, String json, long updateTime) {

		if (dataBaseAdpter == null) {
			return false;
		}

		boolean res = dataBaseAdpter.addBeaconConfig(id, json, updateTime);
		return res;
	}

	/**
	 * 添加Zoneconfig到数据库
	 * 
	 * @param
	 * @return
	 * @date 2014年4月19日
	 * @author trs
	 */
	public boolean addZoneConfig(String id, String json, long updateTime) {

		if (dataBaseAdpter == null) {
			return false;
		}
		boolean res = dataBaseAdpter.addZoneConfig(id, json, updateTime);
		return res;
	}
}
