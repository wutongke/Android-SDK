package com.sensoro.beacon.core;

class ClassTrans {

	/**
	 * 将ActionBrief转换成Action,用于层网络获取ActionBrief后,需要将ActionBrief转换成Action
	 * 
	 * @param
	 * @return
	 * @date 2014年4月15日
	 * @author trs
	 * @throws SensoroException
	 */

	public static Action actionBrief2Action(ActionBrief actionBrief,
			Cache cache, ConfigNetUtils configNetUtils) throws SensoroException {

		if (actionBrief == null || cache == null) {
			return null;
		}

		Action action = new Action();

		action.action = actionBrief.type;
		action.param = actionBrief.param;
		Event event = actionBrief.event;

		if (event != null && event.type != null && event.name != null) {

			String type = actionBrief.event.type;
			String name = actionBrief.event.name;
			BeaconConfig beaconConfig = null;
			// 根据网络返回的Action(实际上是actionBrief)的type和name,获得action的值
			if (type.equals(SensoroSense.SPOT)) {
				if (name.equals(SensoroSense.ENTER)) {
					action.action = SensoroSense.ACTION_ENTER_SPOT;
				} else if (name.equals(SensoroSense.LEAVE)) {
					action.action = SensoroSense.ACTION_LEAVE_SPOT;
				} else if (name.equals(SensoroSense.STAY)) {
					action.action = SensoroSense.ACTION_STAY_SPOT;
				}
			} else if (type.equals(SensoroSense.ZONE)) {
				if (name.equals(SensoroSense.ENTER)) {
					action.action = SensoroSense.ACTION_ENTER_ZONE;
				} else if (name.equals(SensoroSense.LEAVE)) {
					action.action = SensoroSense.ACTION_LEAVE_ZONE;
				} else if (name.equals(SensoroSense.STAY)) {
					action.action = SensoroSense.ACTION_STAY_ZONE;
				}
			}
			beaconConfig = cache.getBeaconConfig(event.id);
			Spot spot = beaconConfig2Spot(beaconConfig);
			action.spot = spot;

			// zid非空,获取zoneConfig.如果spot不属于任何一个区域,则zid为空.
			if (!event.zid.equals("")) {
				ZoneConfig zoneConfig = cache.getZoneConfig(event.zid);
				action.zone = zoneConfigtoZone(zoneConfig);
			}
		}

		return action;
	}

	public static Spot beaconConfig2Spot(BeaconConfig beaconConfig) {

		if (beaconConfig == null || beaconConfig.info == null) {
			return null;
		}
		SpotBrief brief = beaconConfig.info;
		Spot spot = new Spot();
		spot.name = brief.name;
		spot.address = brief.address;
		spot.type = brief.type;
		spot.lat = brief.lat;
		spot.lon = brief.lon;
		spot._date = brief._date;
		spot.param = beaconConfig.param;

		return spot;
	}

	public static Zone zoneConfigtoZone(ZoneConfig zoneConfig) {
		if (zoneConfig == null) {
			return null;
		}
		Zone zone = new Zone();
		zone.zid = zoneConfig.id;
		zone.param = zoneConfig.param;

		return zone;
	}
}
