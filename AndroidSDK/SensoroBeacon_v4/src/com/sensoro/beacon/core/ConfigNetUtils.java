package com.sensoro.beacon.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

class ConfigNetUtils {

	@SuppressWarnings("unused")
	private static final String TAG = ConfigNetUtils.class.getSimpleName();

	public static final String TYPE_STORE = "store";
	public static final String TYPE_BRAND = "brand";

	private static final String BEACON = "beacon";
	private static final String ZONE = "zone";
	private static final String CONFIG = "config";
	private static final String BID = "bid";
	private static final String BRAND = "brand";
	private static final String PARA = "?";
	private static final String EQUAL = "=";
	private static final String SPOT = "spot";
	private static final String LIST = "list";
	private static final String ZIDS = "zids";
	private static final String QUERY = "query";
	private static final String UPDATE = "update";
	private static final String NEARBY = "nearby";
	private static final String LAT = "lat";
	private static final String AND = "&";

	private static final String LON = "lon";
	private UDPHttpUtils udpHttpUtils = null;
	private HttpUtils httpUtils = null;

	private String appID;

	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}

	protected Context context;

	public ConfigNetUtils(Context context, String ip, int port) {
		super();
		this.context = context;
		udpHttpUtils = new UDPHttpUtils(context, ip, port);
		httpUtils = new HttpUtils(context);
	}

	/**
	 * 获取beaconfig
	 * 
	 * @param
	 * @return
	 * @date 2014年2月7日
	 * @author trs
	 * @throws SensoroException
	 */
	public BeaconConfig getBeaconConfig(String uuid, int major, int minor)
			throws SensoroException {

		String path = baseUrlStringBeaconConfig(uuid, major, minor, appID);
		if (path == null) {
			return null;
		}

		String respondString = udpHttpUtils.NetCommunicate(path, null, null,
				UDPHttpUtils.HTTP_GET);
		// String respondString =
		// "[0, [{'id':'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0-0009-0002','sname': 'nothingBeacon','store': {'sid': '3','conf': ['inside']}}, ]]";
		// String respondString =
		// "[0, [{'id':'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0-0009-0002','sname': 'nothingBeacon','store_brand': {'conf': ['credit']}}]]";
		BeaconConfig beaconConfig = BeaconJsonUtils
				.parseBeaconConfig(respondString);
		if (beaconConfig == null) {
			return null;
		}

		return beaconConfig;
	}
	/**
	 * 获取beaconfig
	 * 
	 * @param
	 * @return
	 * @date 2014年2月7日
	 * @author trs
	 * @throws SensoroException
	 */
	public BeaconConfigEx getBeaconConfigEx(String uuid, int major, int minor)
			throws SensoroException {
		
		String path = baseUrlStringBeaconConfig(uuid, major, minor, appID);
		if (path == null) {
			return null;
		}
		
		String respondString = udpHttpUtils.NetCommunicate(path, null, null,
				UDPHttpUtils.HTTP_GET);
		// String respondString =
		// "[0, [{'id':'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0-0009-0002','sname': 'nothingBeacon','store': {'sid': '3','conf': ['inside']}}, ]]";
		// String respondString =
		// "[0, [{'id':'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0-0009-0002','sname': 'nothingBeacon','store_brand': {'conf': ['credit']}}]]";
		BeaconConfigEx beaconConfigEx = BeaconJsonUtils
				.parseBeaconConfigEx(respondString);
		if (beaconConfigEx == null) {
			return null;
		}
		
		return beaconConfigEx;
	}

	/**
	 * 获取beaconfig
	 * 
	 * @param
	 * @return
	 * @date 2014年2月7日
	 * @author trs
	 * @throws SensoroException
	 */
	public BeaconConfig getBeaconConfig(String id) throws SensoroException {

		String path = baseUrlStringBeaconConfig(id, appID);
		if (path == null) {
			return null;
		}

		String respondString = udpHttpUtils.NetCommunicate(path, null, null,
				UDPHttpUtils.HTTP_GET);
		// String respondString =
		// "[0, [{'id':'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0-0009-0002','sname': 'nothingBeacon','store': {'sid': '3','conf': ['inside']}}, ]]";
		// String respondString =
		// "[0, [{'id':'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0-0009-0002','sname': 'nothingBeacon','store_brand': {'conf': ['credit']}}]]";
		BeaconConfig beaconConfig = BeaconJsonUtils
				.parseBeaconConfig(respondString);
		if (beaconConfig == null) {
			return null;
		}

		return beaconConfig;
	}

	/**
	 * 获取ZoneConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年2月7日
	 * @author trs
	 * @throws SensoroException
	 */
	public ArrayList<ZoneConfigEx> getZoneConfigs(List<String> zids)
			throws SensoroException {

		String path = baseUrlStringZoneConfigs(appID);
		if (path == null) {
			return null;
		}

		Map<String, String> bodyMap = new HashMap<String, String>();
		bodyMap.put(ZIDS, zids.toString());
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put("Content-Type", "application/json");

		String respondString = udpHttpUtils.NetCommunicate(path, headersMap,
				bodyMap, UDPHttpUtils.HTTP_POST);
		// String respondString =
		// "[0, [{'id':'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0-0009-0002','sname': 'nothingBeacon','store': {'sid': '3','conf': ['inside']}}, ]]";
		// String respondString =
		// "[0, [{'id':'E2C56DB5-DFFB-48D2-B060-D0F5A71096E0-0009-0002','sname': 'nothingBeacon','store_brand': {'conf': ['credit']}}]]";
		ArrayList<ZoneConfigEx> zoneConfigExs = BeaconJsonUtils
				.parseZoneConfigs(respondString);
		return zoneConfigExs;
	}

	/**
	 * 获取ZoneConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年2月7日
	 * @author trs
	 * @throws SensoroException
	 */
	public ZoneConfig getZoneConfig(String zid) throws SensoroException {

		String path = baseUrlStringZoneConfig(zid, appID);
		if (path == null) {
			return null;
		}

		String respondString = udpHttpUtils.NetCommunicate(path, null, null,
				UDPHttpUtils.HTTP_GET);
		ZoneConfig zoneConfig = BeaconJsonUtils.parseZoneConfig(respondString);
		return zoneConfig;
	}

	private String baseUrlStringBeaconConfig(String uuid, int major, int minor,
			String app) {

		if (uuid == null || app == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(SensoroSense.DEFAULT_PRE_PATH_SDK);
		builder.append(File.separator);
		builder.append(BRAND);
		builder.append(File.separator);
		builder.append(app);
		builder.append(File.separator);
		builder.append(SPOT);
		builder.append(File.separator);
		builder.append(BleEvent.GeneratreID(uuid, major, minor));
		builder.append(File.separator);
		builder.append(CONFIG);

		return builder.toString();
	}

	private String baseUrlStringBeaconConfig(String id, String app) {

		if (id == null || app == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(SensoroSense.DEFAULT_PRE_PATH_SDK);
		builder.append(File.separator);
		builder.append(BRAND);
		builder.append(File.separator);
		builder.append(app);
		builder.append(File.separator);
		builder.append(SPOT);
		builder.append(File.separator);
		builder.append(id);
		builder.append(File.separator);
		builder.append(CONFIG);

		return builder.toString();
	}

	private String baseUrlStringZoneConfigs(String appID) {

		if (appID == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(SensoroSense.DEFAULT_PRE_PATH_SDK);
		builder.append(File.separator);
		builder.append(BRAND);
		builder.append(File.separator);
		builder.append(appID);
		builder.append(File.separator);
		builder.append(ZONE);
		builder.append(File.separator);
		builder.append(CONFIG);
		builder.append(File.separator);
		builder.append(LIST);

		return builder.toString();
	}

	private String baseUrlStringZoneConfig(String zid, String appID) {

		if (appID == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(SensoroSense.DEFAULT_PRE_PATH_SDK);
		builder.append(File.separator);
		builder.append(BRAND);
		builder.append(File.separator);
		builder.append(appID);
		builder.append(File.separator);
		builder.append(ZONE);
		builder.append(File.separator);
		builder.append(zid);
		builder.append(File.separator);
		builder.append(CONFIG);

		return builder.toString();
	}

	private String baseUrlStringOffline(double lat, double lon, long time) {

		if (appID == null) {
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(SensoroSense.DEFAULT_URL);
		builder.append(File.separator);
		builder.append(BRAND);
		builder.append(File.separator);
		builder.append(appID);
		builder.append(File.separator);
		builder.append(SPOT);
		builder.append(File.separator);
		builder.append(QUERY);
		builder.append(File.separator);
		builder.append(NEARBY);
		builder.append(File.separator);
		builder.append(LIST);
		builder.append(PARA);

		addParam(builder, LAT, lat + "");
		builder.append(AND);
		addParam(builder, LON, lon + "");

		return builder.toString();
	}

	private void addParam(StringBuilder builder, String key, String value) {
		if (builder == null || key == null || value == null) {
			return;
		}
		builder.append(key);
		builder.append(EQUAL);
		builder.append(value);
	}

	public BeaconSet getBeaconSet(String appID) {

		BeaconSet beaconSet = null;

		StringBuilder builder = new StringBuilder();
		builder.append(SensoroSense.DEFAULT_PRE_PATH_SDK);
		builder.append(File.separator);
		builder.append(BRAND);
		builder.append(File.separator);
		builder.append(appID);
		builder.append(File.separator);
		builder.append(CONFIG);

		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put("Content-Type", "application/json");

		String respondString = udpHttpUtils.NetCommunicate(builder.toString(),
				headersMap, null, UDPHttpUtils.HTTP_GET);

		beaconSet = BeaconJsonUtils.getBeaconSet(respondString);

		return beaconSet;
	}

	/**
	 * 按照经纬度和最近更新时间查询beacon
	 * 
	 * @param
	 * @return
	 * @date 2014年4月16日
	 * @author trs
	 */
	public String getOfflineBeaconConfigs(double lat, double lon, long time) {

		String url = baseUrlStringOffline(lat, lon, time);
		String respondString = httpUtils.NetCommunicate(url, null, null, null,
				HttpUtils.HTTP_GET);

		return respondString;
	}
}
