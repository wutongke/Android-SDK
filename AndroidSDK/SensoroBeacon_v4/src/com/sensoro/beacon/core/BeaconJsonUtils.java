package com.sensoro.beacon.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

class BeaconJsonUtils {
	private static final String TAG = "BeaconJsonUtils";
	private static final boolean DEBUG = true;
	// BeaconConfig key name
	private static final String _ID = "_id";
	private static final String ID = "id";
	private static final String ZIDS = "zids";
	private static final String INFO = "info";
	private static final String NAME = "name";
	private static final String LAT = "lat";
	private static final String LON = "lon";
	private static final String TYPE = "type";
	private static final String ADDRESS = "address";
	private static final String PARAM = "param";
	private static final String ACTION = "action";
	private static final String EVENT = "event";
	private static final String SPOT = "spot";
	private static final String ZONE = "zone";
	private static final String ACT = "act";
	private static final String _DATE = "_date";

	// BeaconSet
	private static final String SPOT_TIMER = "spotTimer";
	private static final String ZONE_TIMER = "zoneTimer";
	private static final String OFFLINE_DIST = "offlineDist";
	private static final String OFFLINE_TIMER = "offlineTimer";
	private static final String REMOTE = "remote";
	private static final String FREQ = "freq";
	private static final String DAILY = "daily";
	private static final String INTERVAL = "interval";
	private static final String ENTER = "enter";
	private static final String LEAVE = "leave";
	private static final String STAY = "stay";

	public static BeaconConfig parseBeaconConfig(String jsonString)
			throws SensoroException {
		if (jsonString == null) {
			return null;
		}
		JSONTokener tokener = new JSONTokener(jsonString);
		JSONArray jsonArray = null;
		String errInfo = null;
		int state = 0;
		try {
			jsonArray = (JSONArray) tokener.nextValue();
			// 获取业务层响应状态
			state = jsonArray.getInt(0);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (state != 0) {
			// 如果不等于0,则抛出异常
			try {
				if (jsonArray == null) {
					return null;
				} else {
					errInfo = jsonArray.getString(1);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			throw new SensoroException(errInfo);
		}

		BeaconConfig config = new BeaconConfig();
		// 获取beaconConfig接口：即使只有一个也以数组形式返回，取第一个元素就是当前请求的beaconConfig
		JSONArray beaconConfigsArray = jsonArray.optJSONArray(1);
		String beaconConfig = beaconConfigsArray.optString(0);
		config = parseBeaconConfigInfo(beaconConfig);

		return config;
	}

	/**
	 * 将网络返回的BeaconConfig json解析成BeaconConfigEx
	 * 
	 * @param
	 * @return
	 * @date 2014年4月22日
	 * @author trs
	 */
	public static BeaconConfigEx parseBeaconConfigEx(String jsonString)
			throws SensoroException {
		if (jsonString == null) {
			return null;
		}
		JSONTokener tokener = new JSONTokener(jsonString);
		JSONArray jsonArray = null;
		String errInfo = null;
		int state = 0;
		try {
			jsonArray = (JSONArray) tokener.nextValue();
			// 获取业务层响应状态
			state = jsonArray.getInt(0);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (state != 0) {
			// 如果不等于0,则抛出异常
			try {
				if (jsonArray == null) {
					return null;
				} else {
					errInfo = jsonArray.getString(1);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			throw new SensoroException(errInfo);
		}
		BeaconConfigEx beaconConfigEx = new BeaconConfigEx();
		String beaconConfigJson = null;
		BeaconConfig beaconConfig = new BeaconConfig();
		// 获取beaconConfig接口：即使只有一个也以数组形式返回，取第一个元素就是当前请求的beaconConfig
		JSONArray beaconConfigsArray = jsonArray.optJSONArray(1);
		if (beaconConfigsArray != null) {
			beaconConfigJson = beaconConfigsArray.optString(0);
			beaconConfig = parseBeaconConfigInfo(beaconConfigJson);
			beaconConfigEx.beaconConfig = beaconConfig;
			beaconConfigEx.beaconConfigJson = beaconConfigJson;
		}

		return beaconConfigEx;
	}

	public static BeaconConfig parseBeaconConfigInfo(String json)
			throws SensoroException {
		if (json == null) {
			return null;
		}
		JSONObject jsonObject = null;
		BeaconConfig config = new BeaconConfig();
		try {
			jsonObject = new JSONObject(json);
			config = parseBeaconConfigObject(jsonObject);
		} catch (JSONException e) {
			throw new SensoroException(e.getMessage());
		}
		return config;
	}

	private static BeaconConfig parseBeaconConfigObject(JSONObject jsonObject)
			throws SensoroException {

		JSONObject jsonObjectChild = null;
		BeaconConfig config = new BeaconConfig();
		try {
			// act
			jsonObjectChild = jsonObject.optJSONObject(ACT);
			if (jsonObjectChild != null) {
				config.act = parseAct(jsonObjectChild);
			}
			// info
			jsonObjectChild = jsonObject.getJSONObject(INFO);
			if (jsonObjectChild != null) {
				SpotBrief spot = parseSpotBrief(jsonObjectChild);
				config.info = spot;
			}
			// param
			try {
				jsonObjectChild = jsonObject.getJSONObject(PARAM);
				if (jsonObjectChild != null) {
					HashMap<String, String> param = parseHashMap(jsonObjectChild);
					config.param = param;
				}
			} catch (Exception e) {
			}

			// id
			config._id = jsonObject.getString(_ID);

			// zids
			try {
				JSONArray zidsJsonArray = jsonObject.getJSONArray(ZIDS);
				if (config.zids == null) {
					config.zids = new ArrayList<String>();
				}
				for (int i = 0; i < zidsJsonArray.length(); i++) {
					String zid = zidsJsonArray.getString(i);
					config.zids.add(zid);
				}
			} catch (Exception e) {

			}
		} catch (JSONException e) {
			throw new SensoroException(e.getMessage());
		}
		return config;
	}

	public static ArrayList<ZoneConfigEx> parseZoneConfigs(String jsonString)
			throws SensoroException {
		if (jsonString == null) {
			return null;
		}

		JSONTokener tokener = new JSONTokener(jsonString);
		JSONArray jsonArray = null;
		String errInfo = null;
		JSONObject jsonObject = null;
		int state = 0;

		try {
			jsonArray = (JSONArray) tokener.nextValue();
			// 获取业务层响应状态
			state = jsonArray.getInt(0);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (state != 0) {
			// 如果不等于0,则抛出异常
			try {
				if (jsonArray == null) {
					return null;
				} else {
					errInfo = jsonArray.getString(1);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			throw new SensoroException(errInfo);
		}

		ArrayList<ZoneConfigEx> zoneConfigExs = new ArrayList<ZoneConfigEx>();
		ZoneConfigEx zoneConfigEx = null;
		JSONArray zoneConfigsArray = null;
		try {
			zoneConfigsArray = jsonArray.getJSONArray(1);
			for (int i = 0; i < zoneConfigsArray.length(); i++) {
				ZoneConfig zoneConfig = new ZoneConfig();
				jsonObject = zoneConfigsArray.getJSONObject(i);

				zoneConfig = parseZoneConfigObject(jsonObject);

				zoneConfigEx = new ZoneConfigEx();
				zoneConfigEx.zoneConfig = zoneConfig;
				zoneConfigEx.zoneConfigJson = jsonObject.toString();

				zoneConfigExs.add(zoneConfigEx);
			}
		} catch (JSONException e) {
			throw new SensoroException(e.getMessage());
		}
		return zoneConfigExs;
	}

	public static ZoneConfig parseZoneConfig(String jsonString)
			throws SensoroException {
		if (jsonString == null) {
			return null;
		}

		JSONTokener tokener = new JSONTokener(jsonString);
		JSONArray jsonArray = null;
		String errInfo = null;
		JSONObject jsonObject = null;
		int state = 0;

		try {
			jsonArray = (JSONArray) tokener.nextValue();
			// 获取业务层响应状态
			state = jsonArray.getInt(0);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (state != 0) {
			// 如果不等于0,则抛出异常
			try {
				if (jsonArray == null) {
					return null;
				} else {
					errInfo = jsonArray.getString(1);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			throw new SensoroException(errInfo);
		}
		ZoneConfig config = null;
		try {
			jsonObject = jsonArray.getJSONObject(1);
			config = parseZoneConfigObject(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return config;
	}

	public static ZoneConfig parseZoneConfigInfo(String json)
			throws SensoroException {

		if (json == null) {
			return null;
		}

		JSONObject jsonObject = null;
		ZoneConfig config = null;
		JSONTokener tokener = null;
		try {
			tokener = new JSONTokener(json);
			jsonObject = (JSONObject) tokener.nextValue();
			config = parseZoneConfigObject(jsonObject);
		} catch (JSONException e) {
			throw new SensoroException(e.getMessage());
		}

		return config;
	}

	private static ZoneConfig parseZoneConfigObject(JSONObject jsonObject)
			throws SensoroException {

		if (jsonObject == null) {
			return null;
		}
		ZoneConfig zoneConfig = null;
		JSONObject jsonObjectChild = null;
		Act act = null;

		try {
			zoneConfig = new ZoneConfig();
			// act
			jsonObjectChild = jsonObject.getJSONObject(ACT);
			if (jsonObjectChild != null) {
				act = parseAct(jsonObjectChild);
				zoneConfig.act = act;
			}
			// param
			try {
				jsonObjectChild = jsonObject.getJSONObject(PARAM);
				if (jsonObjectChild != null) {
					HashMap<String, String> param = parseHashMap(jsonObjectChild);
					zoneConfig.param = param;
				}
			} catch (Exception e) {
			}

			zoneConfig.id = jsonObject.optString(ID);
			zoneConfig._date = jsonObject.optLong(_DATE);
		} catch (JSONException e) {
			throw new SensoroException(e.getMessage());
		}

		return zoneConfig;
	}

	/**
	 * 解析网络返回的action信息,该action并不是真正要回调给app的action,需要将其中信息完善成实际的对象
	 * 
	 * @param
	 * @return
	 * @date 2014年4月15日
	 * @author trs
	 */
	public static ActionBrief parseBriefAction(String jsonString)
			throws SensoroException {

		if (jsonString == null) {
			return null;
		}

		JSONTokener tokener = new JSONTokener(jsonString);
		JSONArray jsonArray = null;
		String errInfo = null;
		JSONObject jsonObject = null;
		JSONObject jsonObjectChild = null;
		int state = 0;

		try {
			jsonArray = (JSONArray) tokener.nextValue();
			// 获取业务层响应状态
			state = jsonArray.getInt(0);

		} catch (JSONException e) {
			throw new SensoroException(e.getMessage());
		}
		if (state != 0) {
			// 如果不等于0,则抛出异常
			errInfo = jsonArray.optString(1);
			throw new SensoroException(errInfo);
		}
		ActionBrief action = new ActionBrief();

		jsonObject = jsonArray.optJSONObject(1);
		if (jsonObject != null) {
			// type
			action.type = jsonObject.optString(TYPE);
			// param
			jsonObjectChild = jsonObject.optJSONObject(PARAM);
			if (jsonObjectChild != null) {
				HashMap<String, String> param = parseHashMap(jsonObjectChild);
				action.param = param;
			}
			// event
			jsonObjectChild = jsonObject.optJSONObject(EVENT);
			if (jsonObjectChild != null) {
				Event event = new Event();

				event.type = jsonObjectChild.optString(TYPE);
				event.name = jsonObjectChild.optString(NAME);
				event.id = jsonObjectChild.optString(SPOT);
				event.zid = jsonObjectChild.optString(ZONE);

				action.event = event;
			}
		}

		return action;
	}

	// /**
	// * 模拟进店和离店的消息
	// *
	// * @param
	// * @return
	// * @date 2014年3月10日
	// * @author trs
	// */
	// static String simulateInOutMsg(String string) {
	// JSONArray msg = new JSONArray();
	// JSONObject object = new JSONObject();
	// msg.put(0);
	// try {
	// object.put("sid", "1");
	// object.put("message", string);
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// msg.put(object);
	//
	// return msg.toString();
	//
	// }

	public static BeaconSet getBeaconSet(String json) {

		if (json == null) {
			return null;
		}
		BeaconSet beaconSet = new BeaconSet();
		JSONTokener tokener = new JSONTokener(json);
		JSONArray jsonArray = null;
		JSONObject object = null;
		try {
			jsonArray = (JSONArray) tokener.nextValue();
			int state = jsonArray.getInt(0);
			String body = null;
			if (state != 0) {
				// 如果不等于0,则抛出异常
				body = jsonArray.getString(1);
				throw new SensoroException(body);
			}
			object = jsonArray.getJSONObject(1);

			beaconSet.spotTimer = object.getLong(SPOT_TIMER) * 1000;
			beaconSet.zoneTimer = object.getLong(ZONE_TIMER) * 1000;
			beaconSet.offlineDist = object.getLong(OFFLINE_DIST);
			beaconSet.offlineTimer = object.getLong(OFFLINE_TIMER) * 1000;

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			return null;
		}

		return beaconSet;
	}

	/**
	 * 解析Spot
	 * 
	 * @param
	 * @return
	 * @date 2014年4月11日
	 * @author trs
	 */
	private static SpotBrief parseSpotBrief(JSONObject jsonObject) {
		if (jsonObject != null) {
			SpotBrief spot = new SpotBrief();
			spot.name = jsonObject.optString(NAME);
			spot.lat = jsonObject.optDouble(LAT);
			spot.lon = jsonObject.optDouble(LON);
			spot.type = jsonObject.optString(TYPE);
			spot.address = jsonObject.optString(ADDRESS);
			spot._date = jsonObject.optLong(_DATE);

			return spot;
		}
		return null;
	}

	/**
	 * 解析Act
	 * 
	 * @param
	 * @return
	 * @date 2014年4月21日
	 * @author trs
	 */
	private static Act parseAct(JSONObject jsonObject) {

		if (jsonObject == null) {
			return null;
		}
		Act act = null;
		JSONObject tmpObject = null;
		// enter
		tmpObject = jsonObject.optJSONObject(ENTER);
		if (tmpObject != null) {
			if (act == null) {
				act = new Act();
			}
			act.enter = parseActEvent(tmpObject);
		}
		// leave
		tmpObject = jsonObject.optJSONObject(LEAVE);
		if (tmpObject != null) {
			if (act == null) {
				act = new Act();
			}
			act.leave = parseActEvent(tmpObject);
		}
		// enter
		tmpObject = jsonObject.optJSONObject(STAY);
		if (tmpObject != null) {
			if (act == null) {
				act = new Act();
			}
			act.stay = parseActEvent(tmpObject);
		}

		return act;
	}

	/**
	 * 解析ActEvent
	 * 
	 * @param
	 * @return
	 * @date 2014年4月11日
	 * @author trs
	 */
	private static ActEvent parseActEvent(JSONObject jsonObject) {

		JSONObject tmpObject = null;
		if (jsonObject != null) {
			ActEvent actEvent = new ActEvent();
			try {
				actEvent.remote = jsonObject.getBoolean(REMOTE);
				try {
					tmpObject = jsonObject.getJSONObject(FREQ);
					Freq freq = new Freq();
					try {
						freq.daily = tmpObject.getInt(DAILY);
					} catch (JSONException e) {
						freq.daily = -1;
					}
					try {
						freq.interval = tmpObject.getLong(INTERVAL);
					} catch (JSONException e) {
						freq.interval = -1;
					}

					actEvent.freq = freq;
				} catch (Exception e) {
				}
				try {
					actEvent.type = jsonObject.getString(TYPE);
				} catch (Exception e) {
				}
				try {
					tmpObject = jsonObject.getJSONObject(PARAM);
					actEvent.param = parseHashMap(tmpObject);
				} catch (Exception e) {
				}

			} catch (JSONException e) {
				return null;
			}
			return actEvent;
		}
		return null;
	}

	private static HashMap<String, String> parseHashMap(JSONObject jsonObject) {

		if (jsonObject == null) {
			return null;
		}
		HashMap<String, String> map = new HashMap<String, String>();
		Iterator<String> iterator = jsonObject.keys();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			String value;
			try {
				value = jsonObject.getString(key);
			} catch (JSONException e) {
				return null;
			}
			map.put(key, value);
		}
		return map;
	}

	private static void LoggerInfo(String info) {
		if (DEBUG) {
			Log.i(TAG, info);
		}
	}

}
