package com.sensoro.beacon.core;

import java.util.HashMap;
import java.util.LinkedHashMap;

class Fsm {

	static final int LEVEL_ZONE = 0;
	static final int LEVEL_BEACON = 1;
	int levelinner; // ��״̬������ʲô�㼶,�����ڲ�

	boolean inZone;
	long inZoneTime;
	int zoneStayTimes; // ��¼zoneͣ����������

	HashMap<String, Long> inSpotTimeHashMap; // ��¼����Spot��ʱ��
	HashMap<String, Integer> spotStayTimesHashMap; // ��¼spotͣ�������Ĵ���

	String id; // ���ڱ�ʾ״̬��,���id���,��equal��������true
	String level; // ��״̬������ʲô�㼶

	LinkedHashMap<String, BeaconConfig> beaconConfigMap; // �洢�ڸ�fsm��ص�BeaconConfig,����һ��˳���map
	ZoneConfig zoneConfig;

	public Fsm(String id) {

		super();
		this.id = id;
		beaconConfigMap = new LinkedHashMap<String, BeaconConfig>();
		inSpotTimeHashMap = new HashMap<String, Long>();
		spotStayTimesHashMap = new HashMap<String, Integer>();
	}

	/**
	 * ����equal,��store sidһ��ʱ���
	 * 
	 * @param
	 * @return
	 * @date 2014��2��10��
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
		if (!(o instanceof Fsm)) {
			return false;
		}
		if (this.id == null) {
			return false;
		}
		Fsm fsm = (Fsm) o;
		if (this.id.equals(fsm.id)) {
			return true;
		}

		return false;
	}
}
