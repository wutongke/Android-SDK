package com.sensoro.beacon.core;

import java.io.File;

import android.content.Context;
import android.util.Log;

class BeaconHeartBeatUtils {

	private static final String TAG = BeaconHeartBeatUtils.class.getSimpleName();
	private static final boolean DEBUG = true;

	private static final String BRAND = "brand";
	private static final String PARA = "?";
	private static final String HEARTBEAT = "heartbeat";
	private static final String UID = "uid";
	private static final String EQUAL = "=";
	private UDPHttpUtils httpUtils;
	
	private String appID;
	protected Context context;
	
	
	public String getAppID() {
		return appID;
	}

	public void setAppID(String appID) {
		this.appID = appID;
	}

//	public BeaconHeartBeatUtils(Context context) {
//		super();
//		this.context = context;
//		this.addr = SensoroSense.DEFAULT_ADDR;
//		httpsUtils = new HttpsUtils(context);
//	}

	public BeaconHeartBeatUtils(Context context, String ip,int port) {
		super();
		this.context = context;
		httpUtils = new UDPHttpUtils(context,ip,port);
	}

	
	/**
	 * 心跳包
	 * 
	 * @param
	 * @return
	 * @date 2014年2月7日
	 * @author trs
	 * @throws SensoroException 
	 */
	public void HeartBeat(String uid, String appName) throws SensoroException {

		String urlString = BaseUrlString(appName, uid);
		if (urlString == null) {
			return;
		}

		String respondString = httpUtils.NetCommunicate(urlString, null, null, UDPHttpUtils.HTTP_GET);
		if (respondString != null) {
			LoggerInfo(respondString);
		}
		//心跳包不需要接收数据
		//ArrayList<BeaconConfig> configs = BeaconJsonUtils.parseBeaconConfig(respondString);
		
		return;
	}
	private String BaseUrlString(String app, String uid) {
		StringBuilder builder = new StringBuilder();
		builder.append(SensoroSense.DEFAULT_PRE_PATH_SDK);
		builder.append(File.separator);
		builder.append(BRAND);
		builder.append(File.separator);
		builder.append(app);
		builder.append(File.separator);
		builder.append(HEARTBEAT);
		if (uid != null) {
			builder.append(PARA);
			builder.append(UID);
			builder.append(EQUAL);
			builder.append(uid);
		}

		return builder.toString();
	}
	private void LoggerInfo(String info) {
		if (DEBUG) {
			Log.i(TAG, info);
		}
	}
}
