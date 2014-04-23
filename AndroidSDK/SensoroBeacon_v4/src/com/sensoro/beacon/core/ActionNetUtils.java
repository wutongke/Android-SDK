package com.sensoro.beacon.core;

import java.io.File;
import java.util.HashMap;

import android.content.Context;

class ActionNetUtils {

	@SuppressWarnings("unused")
	private static final String TAG = ActionNetUtils.class.getSimpleName();

	private static final String CONNECT_APP = "brand";
	private static final String AND = "&";
	private static final String EQUAL = "=";
	private static final String SPOT = "spot";
	private static final String ZONE = "zone";
	private static final String ASK = "?";
	private static final String UID = "uid";
	private static final String SECONDS = "seconds";
	private static final String UDID = "udid";
	private static final String COOKIE = "Cookie";

	public static final String ENTER = "enter";
	public static final String LEAVE = "leave";
	public static final String STAY = "stay";

	protected Context context;
	protected String uuid;

	private UDPHttpUtils httpUtils = null;

	ActionNetUtils(Context context, String uuid, String ip, int port) {
		super();
		this.context = context;
		this.uuid = uuid;
		httpUtils = new UDPHttpUtils(context, ip, port);
	}

	// static EventActionNetUtils getInstance(Context context, String uuid) {
	// return new EventActionNetUtils(context, uuid);
	// }

	public String spotAction(String appID, String event, long seconds,
			ZoneConfig zoneConfig, BeaconConfig beaconConfig, String uid,
			String uuid) {

		// zoneConfig可能为空(一个spot不属于任何区域),因此需要判断
		String urlString = null;
		if (zoneConfig != null) {
			urlString = baseUrlStringSpot(appID, event, beaconConfig._id,
					zoneConfig.id, seconds, uid);
		} else {
			urlString = baseUrlStringSpot(appID, event, beaconConfig._id, null,
					seconds, uid);
		}

		if (urlString == null) {
			return null;
		}

		String result = null;

		HashMap<String, String> headerMap = new HashMap<String, String>();
		headerMap.put(COOKIE, UDID + "=" + uuid);
		result = httpUtils.NetCommunicate(urlString, headerMap, null,
				UDPHttpUtils.HTTP_GET);
		return result;
	}

	public String zoneAction(String appID, String event, long seconds,
			ZoneConfig zoneConfig, BeaconConfig beaconConfig, String uid,
			String uuid) {

		String urlString = baseUrlStringZone(appID, event, beaconConfig._id,
				zoneConfig.id, seconds, uid);
		if (urlString == null) {
			return null;
		}

		String result = null;
		HashMap<String, String> headerMap = new HashMap<String, String>();
		headerMap.put(COOKIE, UDID + "=" + uuid);
		result = httpUtils.NetCommunicate(urlString, headerMap, null,
				UDPHttpUtils.HTTP_GET);
		return result;
	}

	private String baseUrlStringSpot(String appID, String event, String sid,
			String zid, long seconds, String uid) {

		if (appID == null || event == null || sid == null) {
			return null;
		}

		int paramNum = 0; // 记录参数的个数

		StringBuilder builder = new StringBuilder();
		builder.append(SensoroSense.DEFAULT_PRE_PATH_SDK);
		builder.append(File.separator);
		builder.append(CONNECT_APP);
		builder.append(File.separator);
		builder.append(appID);
		builder.append(File.separator);
		builder.append(SPOT);
		builder.append(File.separator);
		builder.append(sid);
		builder.append(File.separator);
		builder.append(event);
		if (zid != null || seconds != -1 || uid != null) {
			builder.append(ASK);
		}
		if (zid != null) {
			AddParameter(builder, ZONE, zid);
			paramNum++;
		}
		if (seconds != -1) {
			if (paramNum != 0) {
				builder.append(AND);
			}
			AddParameter(builder, SECONDS, seconds + "");
			paramNum++;
		}
		if (uid != null) {
			if (paramNum != 0) {
				builder.append(AND);
			}
			AddParameter(builder, UID, uid);
			paramNum++;
		}

		return builder.toString();
	}

	private String baseUrlStringZone(String appID, String event, String sid,
			String zid, long seconds, String uid) {

		if (appID == null || event == null || zid == null || sid == null) {
			return null;
		}

		int paramNum = 0;
		
		StringBuilder builder = new StringBuilder();
		builder.append(SensoroSense.DEFAULT_PRE_PATH_SDK);
		builder.append(File.separator);
		builder.append(CONNECT_APP);
		builder.append(File.separator);
		builder.append(appID);
		builder.append(File.separator);
		builder.append(ZONE);
		builder.append(File.separator);
		builder.append(zid);
		builder.append(File.separator);
		builder.append(event);
		if (seconds != -1 || uid != null) {
			builder.append(ASK);
		}

		if (seconds != -1) {
			AddParameter(builder, SECONDS, seconds + "");
			paramNum++;
		}
		if (uid != null) {
			if (paramNum != 0) {
				builder.append(AND);
			}
			AddParameter(builder, UID, uid);
			paramNum++;
		}
		return builder.toString();
	}

	private void AddParameter(StringBuilder builder, String key, String value) {
		if (builder == null || value == null) {
			return;
		}
		builder.append(key);
		builder.append(EQUAL);
		builder.append(value);
	}
}
