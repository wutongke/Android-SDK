package com.sensoro.beacon.core;

import java.util.HashMap;
import java.util.UUID;

import org.apache.log4j.Logger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.sensoro.beacon.v4.BuildConfig;

/**
 * ������¼�������
 * 
 * @date 2014��1��21��
 */
public class SensoroSense {

	private static final String TAG = "Events";
	private Logger logger = Logger.getLogger(SensoroSense.class);

	// Action ������action ������
	public static final String ACTION_ENTER_SPOT = "enter_spot";
	public static final String ACTION_LEAVE_SPOT = "leave_spot";
	public static final String ACTION_STAY_SPOT = "stay_spot";
	public static final String ACTION_ENTER_ZONE = "enter_zone";
	public static final String ACTION_LEAVE_ZONE = "leave_zone";
	public static final String ACTION_STAY_ZONE = "stay_zone";
	static final String ZONE = "zone";
	static final String SPOT = "spot";
	static final String ENTER = "enter";
	static final String LEAVE = "leave";
	static final String STAY = "stay";

	protected Context context = null;

	// �¼�������Сʱ�䵥Ԫ,��λ����
	static final long DEFAULT_SPOT_TIMER = 10 * 1000; // �㴥����Ĭ����Сʱ����
	static final long DEFAULT_ZONE_TIMER = 30 * 1000; // ����ͣ��������Ĭ����Сʱ��
	static final long DEFAULT_OFFLINE_TIMER = 30 * 60 * 1000; // �������ݻ�ȡ��ʱ����
	static final long DEFAULT_OFFLINE_DIST = 2 * 1000; // �������ݻ�ȡ�Ĵ�������(��)

	// Ĭ��ɨ��beacon��ʱ����
	static final long DEFAULT_FOREGROUND_SCAN_PERIOD = 1000; // Ĭ��ǰ̨Beaconɨ��ʱ��
	static final long DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD = 1000; // Ĭ��ǰ̨Beaconɨ��ʱ����
	static final long DEFAULT_BACKGROUND_SCAN_PERIOD = 1000; // Ĭ�Ϻ�̨Beaconɨ��ʱ��
	static final long DEFAULT_BACKGROUND_BETWEEN_SCAN_PERIOD = 1000; // Ĭ�Ϻ�̨Beaconɨ��ʱ����
	static final long DEFAULT_DEAL_BEACON_SCAN_PERIOD = 500; // Ĭ�ϴ���beacon�¼���ʱ����
	// ����Ĭ�ϵ�ʱ����
	static final long DEFAULT_HEARTBEAT_PERIOD = 45 * 6 * 1000; // ������Ĭ��ʱ����,4�ְ�
	static final long DEFAULT_FSM_STATUS_PERIOD = 1000; // ���״̬��ʱ����
	static final long NO_BEACONCONFIG_TIMEOUT = 60 * 60 * 1000; // ��ȡ����BeaconConfig��BleEvent�ĳ�ʱʱ��
	static final long NO_ZONECONFIG_TIMEOUT = 60 * 60 * 1000; // ��ȡ����ZoneConfig��BleEvent�ĳ�ʱʱ��
	// ��ȡ��������Ĭ��ʱ����,�ж������Ƿ����ӵļ��
	static final long JUDGE_NETWORK_PERIOD = 5 * 60 * 1000;
	// �Զ���ȡ
	static final long GET_OFFLINE_PERIOD = 60 * 60 * 1000;

	// �����¼���״̬����ʱ(��)
	static final long FSM_TIMEOUT_DEFAULT = 30 * 1000; // Ĭ�ϵ�״̬����ʱ
	static final long BEACONCONFIG_TIMEOUT = 60 * 60 * 1000; // BeaconConfig�ĳ�ʱʱ��
	static final long ZONECONFIG_TIMEOUT = 60 * 60 * 1000; // ZoneConfig�ĳ�ʱʱ��
	// ��ʱ��������,�����������д���,����״̬����״̬��������ɾ��
	static final long BEACON_GONE_TIME = 10 * 1000; // ����ٴ�����conf,���ϴ�����confʱ����������÷�Χ,����Ϊ�߳��˸÷�Χ

	// BleEvent�¼������������
	static final int BLE_EVENT_MAX = 10;

	// UDP
	static final String DEFAULT_ADDR = "117.121.26.61";
	static final int DEFAULT_PORT = 10054;
	static final String DEFAULT_PRE_PATH_SDK = "/v4/sdk";
	// HTTP
	static final String DEFAULT_URL = "http://117.121.26.61/v4/sdk";

	static final String DEAFULT_SDK_LOG_ADDR = "117.121.26.61";
	static final int DEAFULT_SDK_LOG_PORT = 10055;

	// intent ���ݵ�����
	static final String EXTRA_STICKY_FLAG = "EXTRA_STICKY_FLAG";
	static final String EXTRA_PRODUCT_FLAG = "EXTRA_PRODUCT_FLAG";
	public static final String EXTRA_LOCAL_DEBUG_FLAG = "EXTRA_LOCAL_DEBUG_FLAG";
	public static final String EXTRA_REMOTE_DEBUG_FLAG = "EXTRA_REMOTE_DEBUG_FLAG";
	static final String EXTRA_APP_ID = "EXTRA_APP_ID";
	static final String EXTRA_UUID = "EXTRA_UUID";
	static final String EXTRA_BEACON_SET = "EXTRA_BEACON_SET";

	boolean isFsmServiceBind;
	boolean local_log;
	boolean remote_log;
	boolean isSticky;
	boolean isProduct;
	long timeUnit; // �洢�߼���ͣ����������Сʱ�䵥Ԫ

	private static SensoroSense singleEvents;// ����ģʽ
	// Bind��serviceʵ��
	// SService sService;
	IFsmService fsmService;
	// app��uid
	String uid;
	// appID
	String appID;
	// action����ͨ�ŵ���
	ActionNetUtils eventActionNetUtils;

	// UUID ��������ͨ��
	private String uuid;
	private String appKey;
	// appName
	static final String UID = "uid";
	static final String UUID = "uuid";

	// BeaconSet,�������ȡ��Ĭ�ϴ���ʱ��;�����Ϣ
	BeaconSet beaconSet;
	ConfigNetUtils configNetUtils;
	// UPD����
	AndroidUDP logUdp;
	Handler udpLoghandler;
	UdpLogRunnable udpLogRunnable;
	Thread udpLogthrThread;
	private static final int MSG_UDP = 0;
	// �洢�������ݵ�SharedPreferences
	private SharedPreferences sharedPreferences;
	static final String SENSORO_SENSE_PREF = "SENSORO_SENSE_PREF";
	static final String SPOT_TIMER = "SPOT_TIMER";
	static final String ZONE_TIMER = "ZONE_TIMER";
	static final String OFFLINE_TIMER = "OFFLINE_TIMER";
	static final String OFFLINE_DIST = "OFFLINE_DIST";
	static final String PREF_UUID = "PREF_UUID";
	static final String LAST_OFFLINE_DATA_TIME = "LAST_OFFLINE_DATA_TIME"; // �洢�ϴλ�ȡ�������ݳɹ���ʱ���Key
	static final String IS_LOCATION_TO_GET = "IS_LOCATION_TO_GET"; // �洢�Ƿ��Ƕ�λ����Ļ�ȡ������������

	private SensoroSense(Context context, String appID, String appKey) {
		super();
		this.context = context;
		this.appID = appID;
		this.appKey = appKey;
		initEventTriggerTimeUnit(context);
		initUUID();
		logUdp = new AndroidUDP(context, DEAFULT_SDK_LOG_PORT,
				DEAFULT_SDK_LOG_ADDR, 3000, AndroidUDP.SOCKET_MULTIPLY);

	}

	/**
	 * ��ʼ��SharePreference,��ȡ��С�¼�������λ
	 * 
	 * @return
	 * @date 2014��4��14��
	 * @author zwz
	 */
	private void initEventTriggerTimeUnit(Context context) {
		if (sharedPreferences == null) {
			initSharedPreferences();
		}

		timeUnit = sharedPreferences.getLong(SPOT_TIMER, 10 * 1000);
	}

	public static SensoroSense getIntance(Context context, String appID,
			String appKey) {
		if (appID == null) {
			return null;
		}

		if (singleEvents == null) {
			singleEvents = new SensoroSense(context, appID, appKey);
		}

		return singleEvents;
	}

	private void initUDPLogThread() {
		if (udpLogRunnable == null) {
			udpLogRunnable = new UdpLogRunnable();
			udpLogthrThread = new Thread(udpLogRunnable);
			udpLogthrThread.start();
		}
	}

	/**
	 * ��ʼ��sdk��uuid,
	 * 
	 * @param
	 * @return
	 * @date 2014��2��18��
	 * @author trs
	 */
	private void initUUID() {

		// ��sharedPreferences��ȡuuid
		uuid = sharedPreferences.getString(PREF_UUID, null);
		if (context == null) {
			return;
		}
		if (uuid == null) {
			String androidId = android.provider.Settings.Secure.getString(
					context.getContentResolver(),
					android.provider.Settings.Secure.ANDROID_ID);
			UUID deviceUuid = new UUID(androidId.hashCode(), System.nanoTime());
			uuid = deviceUuid.toString();

			String log = LogUtils.getCurrentLog("uuid" + uuid);
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}
			// �洢uuid
			Editor editor = sharedPreferences.edit();
			editor.putString(PREF_UUID, uuid);
			editor.commit();
		}

	}

	/**
	 * bind event
	 * 
	 * @param Context
	 *            context context
	 * @param Intent
	 *            intent �����̳���FsmService��Service��intent
	 * @param boolean isSticky ������̹ر��Ժ��Ƿ�������������
	 * @param logStatusMap
	 *            key : local_log ���ص�����Ϣ�Ƿ���,Ϊtrue����; remote_log :
	 *            Զ�̵�����Ϣ������־,Ϊtrue����
	 * 
	 * @return
	 * @date 2014��1��21��
	 * @author trs
	 */
	public void startService(Context context, Intent intent, boolean isSticky,
			HashMap<String, Boolean> logStatusMap) {

		this.context = context;
		this.isSticky = isSticky;

		if (logStatusMap != null && logStatusMap.size() > 0) {
			this.local_log = logStatusMap.get(EXTRA_LOCAL_DEBUG_FLAG);
			this.remote_log = logStatusMap.get(EXTRA_REMOTE_DEBUG_FLAG);
		} else {
			this.local_log = true;
			this.remote_log = true;
		}
		this.isProduct = !BuildConfig.DEBUG;

		if (local_log) {
			AndroidLog4J.InitLog4j();
		}
		if (remote_log) {
			initUDPLogThread();
		}

		String log = LogUtils.getCurrentLog();
		if (local_log) {
			logger.debug(log);
		}
		if (remote_log) {
			sendUdpLogMsg(log);
		}
		// ��ʼ�����繤����
		configNetUtils = new ConfigNetUtils(context, DEFAULT_ADDR, DEFAULT_PORT);
		// ��ȡ��ǰ��BeaconSet
		beaconSet = getCurrentBeaconSet();
		// ������ȡBeaconSet���߳�
		startGetBeaconSetThread();
		// ��������
		startBeaconService(context, intent, isSticky, !BuildConfig.DEBUG,
				logStatusMap, beaconSet);
		// startEventTriggerTimeUnitThread();
	}

	/**
	 * ��ȡ��ǰ��BeaconSet,BeaconSet��ȡ�Ĺ����ǵ��λ�ȡ�´�ʹ��
	 * 
	 * @param
	 * @return
	 * @date 2014��4��23��
	 * @author trs
	 */
	private BeaconSet getCurrentBeaconSet() {

		if (sharedPreferences == null) {
			initSharedPreferences();
		}
		BeaconSet beaconSet = new BeaconSet();
		beaconSet.spotTimer = sharedPreferences.getLong(SPOT_TIMER,
				DEFAULT_SPOT_TIMER);
		beaconSet.zoneTimer = sharedPreferences.getLong(ZONE_TIMER,
				DEFAULT_ZONE_TIMER);
		beaconSet.offlineTimer = sharedPreferences.getLong(OFFLINE_TIMER,
				DEFAULT_OFFLINE_TIMER);
		beaconSet.offlineDist = sharedPreferences.getLong(OFFLINE_DIST,
				DEFAULT_OFFLINE_DIST);

		return beaconSet;
	}

	/**
	 * ������ȡ��С����ʱ���Լ��������ݻ�ȡʱ��;�����߳�
	 * 
	 * @param
	 * @return
	 * @date 2014��4��14��
	 * @author zwz
	 */
	private void startGetBeaconSetThread() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				initBeaconSet();
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	public void stopService(Intent intent) {
		if (context != null) {
			context.stopService(intent);
		}
		context.unbindService(serviceConnection);
		this.context = null;
	}

	/**
	 * ���û���UID
	 * 
	 * @param
	 * @return
	 * @date 2014��2��18��
	 * @author trs
	 */
	public void bindUid(String uid) {
		this.uid = uid;
		if (fsmService != null) {
			try {
				fsmService.setUID(uid);
			} catch (RemoteException e) {
				e.printStackTrace();
				Log.e(TAG, "bindUid err -- " + e.getMessage());
			}
		}
	}

	// /**
	// * ���û���UID
	// *
	// * @param
	// * @return
	// * @date 2014��2��18��
	// * @author trs
	// */
	// private void bindUUID(String uuid) {
	// if (fsmService != null) {
	// try {
	// fsmService.setUUID(uuid);
	// } catch (RemoteException e) {
	// e.printStackTrace();
	// Log.e(TAG, "bindUid err -- " + e.getMessage());
	// }
	// }
	// }

	private void sendUdpLogMsg(String log) {

		if (udpLoghandler == null) {
			return;
		}
		Message msg = udpLoghandler.obtainMessage(MSG_UDP, log);
		udpLoghandler.sendMessage(msg);
	}

	/**
	 * ��ȡactiveAction����ͨ����
	 * 
	 * @param
	 * @return
	 * @date 2014��2��18��
	 * @author trs
	 */
	public SensoroService getSensoroService(Context context) {
		// ������֤uid
		if (uid == null) {
			return null;
		}
		return new SensoroService(context, uuid, uid, appID);
	}

	/**
	 * ������̨Service
	 * 
	 * @param
	 * @return
	 * @date 2014��2��5��
	 * @author trs
	 */
	private void startBeaconService(Context context, Intent intent,
			boolean isSticky, boolean isProduct,
			HashMap<String, Boolean> logStatusMap, BeaconSet beaconSet) {

		String log = null;
		if (context == null) {
			log = LogUtils.getCurrentLog("context null");
			logger.debug(log);
			sendUdpLogMsg(log);
			return;
		}

		if (appID == null) {
			log = LogUtils.getCurrentLog("appID null");
			logger.debug(log);
			sendUdpLogMsg(log);
			return;
		}

		if (beaconSet == null) {
			log = LogUtils.getCurrentLog("beaconSet null");
			logger.debug(log);
			sendUdpLogMsg(log);
			return;
		}

		log = LogUtils.getCurrentLog("start service");
		logger.debug(log);
		sendUdpLogMsg(log);

		// start Ble Service
		Intent bleIntent = new Intent();
		bleIntent.setClass(context, BleService.class);
		context.startService(bleIntent);

		// start FsmService
		intent.putExtra(EXTRA_STICKY_FLAG, isSticky);
		intent.putExtra(EXTRA_APP_ID, appID);
		intent.putExtra(EXTRA_UUID, uuid);
		intent.putExtra(EXTRA_PRODUCT_FLAG, isProduct);
		intent.putExtra(EXTRA_BEACON_SET, beaconSet);
		intent.putExtra(EXTRA_LOCAL_DEBUG_FLAG, local_log);
		intent.putExtra(EXTRA_REMOTE_DEBUG_FLAG, remote_log);

		log = LogUtils.getCurrentLog(beaconSet.toString());
		if (local_log) {
			logger.debug(log);
		}
		if (remote_log) {
			sendUdpLogMsg(log);
		}

		context.startService(intent);
		if (!isFsmServiceBind) {
			log = LogUtils.getCurrentLog("bindService");
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}

			context.bindService(intent, serviceConnection,
					Context.BIND_AUTO_CREATE);
		} else {
			log = LogUtils.getCurrentLog("FsmService has bean bind");
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}
		}
	}

	private void initSharedPreferences() {
		sharedPreferences = context.getSharedPreferences(SENSORO_SENSE_PREF,
				Context.MODE_PRIVATE);
	}

	private void initBeaconSet() {

		BeaconSet beaconSet = configNetUtils.getBeaconSet(appID);

		if (beaconSet == null) {
			beaconSet = new BeaconSet();
			beaconSet.spotTimer = SensoroSense.DEFAULT_SPOT_TIMER;
			beaconSet.zoneTimer = SensoroSense.DEFAULT_ZONE_TIMER;
			beaconSet.offlineTimer = SensoroSense.DEFAULT_OFFLINE_TIMER;
			beaconSet.offlineDist = SensoroSense.DEFAULT_OFFLINE_DIST;
		}
		// �洢BeaconSet
		if (sharedPreferences == null) {
			initSharedPreferences();
		}
		Editor editor = sharedPreferences.edit();
		editor.putLong(SPOT_TIMER, beaconSet.spotTimer);
		editor.putLong(ZONE_TIMER, beaconSet.zoneTimer);
		editor.putLong(OFFLINE_DIST, beaconSet.offlineDist);
		editor.putLong(OFFLINE_TIMER, beaconSet.offlineTimer);
		editor.commit();
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

			fsmService = IFsmService.Stub.asInterface(service);
			try {
				String log = LogUtils
						.getCurrentLog("IFsmService onServiceConnected initHeartBeat");
				if (local_log) {
					logger.debug(log);
				}
				if (remote_log) {
					sendUdpLogMsg(log);
				}

				fsmService.initHeartBeat();
				if (uid != null) {
					fsmService.setUID(uid);
				}
				if (uuid != null) {
					fsmService.setUUID(uuid);
				}
				isFsmServiceBind = true;
			} catch (RemoteException e) {

			}

		}
	};

	/**
	 * �ж��Ƿ����Խ����
	 * 
	 * @param
	 * @return
	 * @date 2014��2��28��
	 * @author trs
	 */
	public Zone[] getZones() {
		if (fsmService == null) {
			return null;
		}
		Zone[] zones = null;
		try {
			zones = fsmService.getZones();
		} catch (RemoteException e) {
			return null;
		}
		return zones;
	}

	/**
	 * �ж��Ƿ��ڿ�ʹ���Ż݄�������
	 * 
	 * @param
	 * @return
	 * @date 2014��2��28��
	 * @author trs
	 */
	public Spot[] getSpots() {
		if (fsmService == null) {
			return null;
		}
		Spot[] spots = null;
		try {
			spots = fsmService.getSpot();
		} catch (RemoteException e) {
			return null;
		}
		return spots;
	}

	public class SensoroService {

		// private static final String TAG =
		// EventActionNetUtils.class.getSimpleName();

		public static final String STORE = "store";
		public static final String BRAND = "brand";

		protected Context context;
		protected String addr;
		protected String uuid;
		protected String uid;
		protected String appID;

		SensoroService(Context context, String uuid, String uid, String appID) {
			super();
			this.context = context;
			addr = DEFAULT_ADDR;
			this.uuid = uuid;
			this.uid = uid;
			this.appID = appID;
		}

		// /**
		// * brand���������
		// *
		// * @param
		// * @return
		// * @date 2014��2��7��
		// * @author trs
		// */
		// private String Connect(String method, String bid, String type, String
		// name,
		// String uid, String id, String sid) {
		//
		// String urlString = BaseUrlString(bid, type, name, uid, id, sid,
		// uuid);
		// if (urlString == null) {
		// return null;
		// }
		//
		// String result = httpsUtils.NetCommunicate(urlString, null, null,
		// null,
		// null);
		//
		// return result;
		// }
	}

	class UdpLogRunnable implements Runnable {

		@Override
		public void run() {
			Looper.prepare();

			udpLoghandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					int what = msg.what;
					String log = (String) msg.obj;
					if (log == null || logUdp == null) {
						return;
					}
					switch (what) {
					case MSG_UDP:
						LogUtils.updLog(logUdp, log);
						break;

					default:
						break;
					}

					super.handleMessage(msg);
				}
			};
			Looper.loop();
		}

	}
}
