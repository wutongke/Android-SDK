package com.sensoro.beacon.core;

import java.util.HashMap;
import java.util.LinkedHashMap;

class Fsm {

	static final int LEVEL_ZONE = 0;
	static final int LEVEL_BEACON = 1;
	int levelinner; // 该状态机数据什么层级,用于内部

	boolean inZone;
	long inZoneTime;
	int zoneStayTimes; // 记录zone停留触发次数

	HashMap<String, Long> inSpotTimeHashMap; // 记录进入Spot的时间
	HashMap<String, Integer> spotStayTimesHashMap; // 记录spot停留触发的次数

	String id; // 用于标示状态机,如果id相等,则equal函数返回true
	String level; // 该状态机数据什么层级

	LinkedHashMap<String, BeaconConfig> beaconConfigMap; // 存储于该fsm相关的BeaconConfig,这是一个顺序的map
	ZoneConfig zoneConfig;

	public Fsm(String id) {

		super();
		this.id = id;
		beaconConfigMap = new LinkedHashMap<String, BeaconConfig>();
		inSpotTimeHashMap = new HashMap<String, Long>();
		spotStayTimesHashMap = new HashMap<String, Integer>();
	}

	/**
	 * 重载equal,当store sid一致时相等
	 * 
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
