package com.sensoro.beacon.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

class OfflineJsonDBUtils {

	private static final String TAG = OfflineJsonDBUtils.class.getSimpleName();

	private static final String _ID = "_id";
	private static final String SPOTS = "spots";
	private static final String ZONES = "zones";
	private static final String INFO = "";
	private static final String _DATE = "_date";
	private static final String ID = "id";
	private static final boolean DEBUG = true;

	/**
	 * 将网络获取的离线Beaconconfig和ZoneConfig数据存储到本地,该函数中使用的增删改查方法全部是没有锁的
	 * 
	 * @param
	 * @return 如果存储成功返回true,否则返回false
	 * @date 2014年4月15日
	 * @author trs
	 */
	public static boolean saveConfigs(String json, DataBaseAdpter dataBaseAdpter)
			throws SensoroException {

		if (dataBaseAdpter == null || json == null) {
			return false;
		}

		JSONTokener tokener = new JSONTokener(json);
		JSONArray jsonArray = null;
		JSONObject object = null;
		JSONObject childObject = null;
		JSONObject tmpObject = null;
		JSONArray spotsJsonArray = null;
		JSONArray zonesJsonArray = null;
		int saveBeaconConfigCount = 0;
		int saveZoneConfigCount = 0;
		BeaconConfigEx beaconConfigEx = null;
		ZoneConfigEx zoneConfigEx = null;
		boolean res = false;
		int ret = -1;
		long _date = 0;

		try {
			jsonArray = (JSONArray) tokener.nextValue();
			int state = jsonArray.getInt(0);
			String body = null;
			String info = null;
			String _id = null;
			String id = null;
			if (state != 0) {
				// 如果不等于0,则抛出异常
				body = jsonArray.getString(1);
				throw new SensoroException(body);
			}
			try {
				object = jsonArray.getJSONObject(1);
			} catch (JSONException e) {
				loggInfo("json String err");
				return false;
			}

			int pos = 0;
			// 该锁防止正在进行数据库增删改查时对数据库进行更新操作
			synchronized (dataBaseAdpter.lockObject) {
				dataBaseAdpter.lockOfflineWorking();
				// 处理BeaconConfigs
				try {
					spotsJsonArray = object.getJSONArray(SPOTS);
				} catch (JSONException e) {
				}
				if (spotsJsonArray != null) {
					dataBaseAdpter.sqliteDatabase.beginTransaction();
					while (true) {
						childObject = spotsJsonArray.optJSONObject(pos);
						if (childObject == null) {
							break;
						}
						// _id
						_id = childObject.optString(_ID);
						// info
						tmpObject = childObject.optJSONObject(INFO);
						if (tmpObject != null) {
							_date = tmpObject.optLong(_DATE);
						}

						// 查询该beacon id信息在数据库中是否存在。若存在，则更新；不存在，则添加
						if (_id != null) {
							beaconConfigEx = dataBaseAdpter
									.getBeaconConfigInfoNoLock(_id);
							info = childObject.toString();
							if (beaconConfigEx == null) {
								res = dataBaseAdpter.addBeaconConfigNoLock(_id,
										info, _date);
								if (res) {
									saveBeaconConfigCount++;
								} else {
									// add failure
									loggInfo("add BeaconConfig failure id - "
											+ _id + " info - " + info);
								}
							} else {
								// 判断更新时间,如果更新时间不相等,则更新数据库
								if (beaconConfigEx._date != _date) {
									ret = dataBaseAdpter
											.updateBeaconConfigNoLock(_id,
													info, _date);
									if (ret > 0) {
										saveBeaconConfigCount++;
									} else {
										// update failure
										loggInfo("update BeaconConfig failure id - "
												+ _id + " info - " + info);
									}
								} else {
									// 更新时间相同,不需要更新数据库
									loggInfo("no need to update BeaconConfig");
								}

							}
						}

						pos++;
					}
					loggInfo("update or add BeaconConfig count -"
							+ saveBeaconConfigCount);
				}

				// 处理ZoneConfigs
				try {
					zonesJsonArray = object.getJSONArray(ZONES);
				} catch (JSONException e) {
				}
				if (zonesJsonArray != null) {
					pos = 0;
					while (true) {
						childObject = zonesJsonArray.optJSONObject(pos);
						if (childObject == null) {
							break;
						}
						// id
						id = childObject.optString(ID);
						// info
						_date = childObject.optLong(_DATE);
						if (id != null) {
							// 先查询数据库中是否有对应的ZoneConfig,如果有则更新
							zoneConfigEx = dataBaseAdpter
									.getZoneConfigInfoNoLock(id);
							info = childObject.toString();
							if (zoneConfigEx == null) {
								// 没有对应ZoneConfig
								res = dataBaseAdpter.addZoneConfigNoLock(id,
										info, _date);
								if (res) {
									saveZoneConfigCount++;
								} else {
									// add failure
									loggInfo("add ZoneConfig failure id - "
											+ id + " info - " + info);
								}
							} else {
								if (zoneConfigEx._date != _date) {
									// 如果更新时间不同,则更新ZoneConfig
									ret = dataBaseAdpter
											.updateZoneConfigNoLock(id, info,
													_date);
									if (ret > 0) {
										saveZoneConfigCount++;
									} else {
										// update failure
										loggInfo("update ZoneConfig failure id - "
												+ id + " info - " + info);
									}
								} else {
									// 跟时间相同,则不需要更新ZoneConfig
									loggInfo("no need to update ZoneConfig");
								}

							}
						}

						pos++;
					}
					loggInfo("update or add ZoneConfig count -"
							+ saveZoneConfigCount);
				}
			}
		} catch (JSONException e) {
			dataBaseAdpter.sqliteDatabase.endTransaction();
			dataBaseAdpter.unlockOfflineWorking();
			return false;
		}

		dataBaseAdpter.sqliteDatabase.setTransactionSuccessful();
		dataBaseAdpter.sqliteDatabase.endTransaction();
		dataBaseAdpter.unlockOfflineWorking();
		return true;
	}

	private static void loggInfo(String log) {
		if (DEBUG) {
			if (log != null) {
				Log.i(TAG, log);
			}
		}
	}
}
