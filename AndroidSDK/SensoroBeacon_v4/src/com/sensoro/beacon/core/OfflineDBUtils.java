package com.sensoro.beacon.core;

public class OfflineDBUtils {
	DataBaseAdpter dataBaseAdpter;

	public OfflineDBUtils(DataBaseAdpter dataBaseAdpter) {
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
		BeaconConfig beaconConfig = BeaconJsonUtils.parseBeaconConfigInfo(info);

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
		ZoneConfig zoneConfig = BeaconJsonUtils.parseZoneConfigInfo(info);

		return zoneConfig;
	}

	/**
	 * �����ݿ��ȡActionRecord
	 * 
	 * @param
	 * @return
	 * @date 2014��4��17��
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
	 * ���ActionRecord��¼
	 * 
	 * @param
	 * @return
	 * @date 2014��4��17��
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
	 * ����ActionRecord��¼
	 * 
	 * @param
	 * @return
	 * @date 2014��4��17��
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
	 * ���BeaconConfig�����ݿ�
	 * 
	 * @param
	 * @return
	 * @date 2014��4��19��
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
	 * ���Zoneconfig�����ݿ�
	 * 
	 * @param
	 * @return
	 * @date 2014��4��19��
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
