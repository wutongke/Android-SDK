package com.sensoro.beacon.core;

class ClassTrans {

	/**
	 * ��ActionBriefת����Action,���ڲ������ȡActionBrief��,��Ҫ��ActionBriefת����Action
	 * 
	 * @param
	 * @return
	 * @date 2014��4��15��
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
			// �������緵�ص�Action(ʵ������actionBrief)��type��name,���action��ֵ
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

			// zid�ǿ�,��ȡzoneConfig.���spot�������κ�һ������,��zidΪ��.
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
