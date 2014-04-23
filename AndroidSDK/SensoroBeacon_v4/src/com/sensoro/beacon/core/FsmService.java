package com.sensoro.beacon.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

/**
 * �û���Ҫ�̳и��������ɶ�Ӧ�Ĺ���
 * 
 * @param
 * @return
 * @date 2014��3��11��
 * @author trs
 */
public abstract class FsmService extends Service {

	private static final boolean DEBUG = true;
	Logger logger = Logger.getLogger(FsmService.class);

	private static final int BLE_EVENT_MSG_NEW = 0;
	private static final int BLE_EVENT_MSG_GONE = 1;

	private static final String TAG = FsmService.class.getSimpleName();
	private Cache cache; // ����

	private String appID;
	private String uid;
	private String uuid;
	private boolean isSticky;
	private boolean local_log;
	private boolean remote_log;
	// ����ͨ����
	private ConfigNetUtils configNetUtils;
	// BleEventֹͣ��־
	private boolean stopFlag = false;
	// �����߳�
	private Thread heartBeatThread;
	private HeartBeatRunnable heartBeatRunnable;
	private boolean heartBeatStopFlag;
	private BeaconHeartBeatUtils heartBeatUtils;

	// ״̬��״̬����߳�
	private Thread fsmStatusThread;
	private FsmStatusRunnable fsmStatusRunnable;

	// �ж��Ƿ������߳�
	private Thread networkAvailableThread;
	private NetworkAvailableRunnable networkAvailableRunnable;
	// �Ƿ�������־λ
	// private boolean isNetValidFlag = false;
	// �洢�ϴζ�λ��location�Ͷ�λʱ��
	double lastLocationLat = 0;
	double lastLocationLon = 0;
	long lastLocationTime;
	// action��������Լ�����Action�㲥���߳�
	private Thread actionThread;
	private ActionRunnable actionRunnable;
	private Handler actionHandler;
	// action����ͳ���߳�,��������ģʽ
	private Thread actionCountThread;
	private ActionCountRunnable actionCountRunnable;
	private Handler actionCountHandler;
	// ��Ϣ��� action������ʺ�action����ͳ���̹߳���
	private static final int MSG_ENTER_SPOT_ACTION = 0;
	private static final int MSG_LEAVE_SPOT_ACTION = 1;
	private static final int MSG_STAY_SPOT_ACTION = 2;
	private static final int MSG_ENTER_ZONE_ACTION = 3;
	private static final int MSG_LEAVE_ZONE_ACTION = 4;
	private static final int MSG_STAY_ZONE_ACTION = 5;
	// BleEvent�㲥���������յ�BleEvent����BleEvent���߳�
	private Handler bleEventHandler;
	private Thread bleEventThread;
	private BleEventRunnable bleEventRunnable;
	// Event������߳�
	private Handler eventHandler;
	private Thread eventThread;
	private EventRunnable eventRunnable;
	private ActionNetUtils netUtils;
	private static final int MSG_ENTER_SPOT = 0;
	private static final int MSG_LEAVE_SPOT = 1;
	private static final int MSG_STAY_SPOT = 2;
	private static final int MSG_ENTER_ZONE = 3;
	private static final int MSG_LEAVE_ZONE = 4;
	private static final int MSG_STAY_ZONE = 5;
	// action��������Լ�����Action�㲥���߳�
	private Thread beaconDiscoveryThread;
	private BeaconDiscoveryRunnable beaconDiscoveryRunnable;
	private Handler beaconDiscoveryHandler;
	private static final int MSG_NEW_BEACON = 1;
	private static final int MSG_GONE_BEACON = 2;

	private static final String ZONE = "ZONE";
	private static final String SPOT = "SPOT";
	private static final String ZONE_CONFIG = "ZONE_CONFIG";
	private static final String BEACON_CONFIG = "BEACON_CONFIG";
	private static final String SECONDS = "SECONDS";

	BeaconStatusBroadcastReceiver receiver;

	// �Ƿ����������ݵı�־
	boolean isOffline = false;
	// SDK SharedPreferences,�������ڴ洢�ϴλ�ȡ�������ݵ�ʱ��
	private SharedPreferences sharedPreferences;
	// BeaconSet,�������ȡ��Ĭ�ϴ���ʱ��;�����Ϣ
	BeaconSet beaconSet;

	// UPD����
	AndroidUDP udp;
	Handler udpLoghandler;
	UdpLogRunnable udpLogRunnable;
	Thread udpLogthrThread;
	private static final int MSG_UDP = 0;

	// λ�ö�λ�ص�
	OfflineLocationListener locationListener;
	LocationManager locationManager;
	Thread locationThread;
	Handler locationHandler;
	LocationRunnable locationRunnable;
	private static final int MSG_LOCATE = 0;
	private static final String LAT = "LAT";
	private static final String LON = "LON";
	private static final String TIMESTAMP = "TIMESTAMP";

	// �����������ݿ�
	DataBaseAdpter dataBaseAdpter;
	OfflineDBUtils offileDBUtils;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		getExtraData(intent);
		if (appID == null) {
			logger.debug("appID null");
			return START_NOT_STICKY;
		}
		// ��ʼ������
		initUtils();
		// ��ʼ����������
		initContainer();
		// ������Ϣ��ʼ��
		if (local_log) {
			initUDPLogThread();
		}
		initBleEventThread();
		// ��ʼ���㲥������
		initBroadcastReceiver();
		// action�߳�
		initActionThread();
		// actionͳ���߳�
		initActionCountThread();
		// ���ֺ���ʧBeacon�߳�
		initBeaconDiscoveryThread();
		// Event �߳�
		initEventThread();
		// ��ʼ��״̬��״̬����߳�
		initFsmStatusThread();
		// ��ʼ����λ
		initLocation();
		// ��ʼ���������ݿ�
		initOfflineDB();
		// �����������ݶ�ʱ��ȡ�߳�
		initOfflineDataThread();

		if (isSticky) {
			return START_REDELIVER_INTENT;
		} else {
			return START_NOT_STICKY;
		}
	}

	private void getExtraData(Intent intent) {
		isSticky = intent
				.getBooleanExtra(SensoroSense.EXTRA_STICKY_FLAG, false);
		local_log = intent.getBooleanExtra(SensoroSense.EXTRA_LOCAL_DEBUG_FLAG,
				false);
		appID = intent.getStringExtra(SensoroSense.EXTRA_APP_ID);
		uuid = intent.getStringExtra(SensoroSense.EXTRA_UUID);
		beaconSet = intent.getParcelableExtra(SensoroSense.EXTRA_BEACON_SET);
	}

	private void initContainer() {
		if (cache == null) {
			cache = new Cache();
		}
	}

	private void initBleEventThread() {
		if (bleEventRunnable == null) {
			bleEventRunnable = new BleEventRunnable();
		}

		if (bleEventThread == null) {
			String log = null;
			if (local_log || remote_log) {
				log = LogUtils.getCurrentLog();
			}
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}
			bleEventThread = new Thread(bleEventRunnable);
			bleEventThread.start();
		}
	}

	private void initFsmStatusThread() {

		if (fsmStatusRunnable == null) {
			String log = null;
			if (local_log || remote_log) {
				log = LogUtils.getCurrentLog();
			}
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}
			fsmStatusRunnable = new FsmStatusRunnable();
			fsmStatusThread = new Thread(fsmStatusRunnable);

			fsmStatusThread.start();
		}
	}

	/**
	 * ��ʼ��λ�ûص����
	 * 
	 * @param
	 * @return
	 * @date 2014��4��15��
	 * @author trs
	 */
	private void initLocation() {
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		locationListener = new OfflineLocationListener();
		locationRunnable = new LocationRunnable();
		locationThread = new Thread(locationRunnable);
		locationThread.start();
		startLocation();
	}

	private void startLocation() {
		// ��ȡλ�ù������
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		// ���ҵ�������Ϣ
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE); // �;���
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW); // �͹���

		String provider = locationManager.getBestProvider(criteria, true); // ��ȡGPS��Ϣ
		// Location location = locationManager.getLastKnownLocation(provider);
		// // ͨ��GPS��ȡλ��
		// ���ü��������Զ����µ���Сʱ��Ϊ���N��(1��Ϊ1*1000������д��ҪΪ�˷���)����Сλ�Ʊ仯����N��
		locationManager
				.requestLocationUpdates(provider, beaconSet.offlineTimer,
						beaconSet.offlineDist, locationListener);
	}

	private void initHeartBeatThread() {

		if (isOffline) {
			String log = null;
			if (local_log || remote_log) {
				log = LogUtils
						.getCurrentLog("offline mode do not have heartbeat");
			}
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}
			// ���߹��ܲ���Ҫ��������
			return;
		}
		if (heartBeatRunnable == null) {

			String log = null;
			if (local_log || remote_log) {
				log = LogUtils.getCurrentLog();
			}
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}
			if (heartBeatUtils == null) {
				// ���������Ͳ��Ի���
				heartBeatUtils = new BeaconHeartBeatUtils(this,
						SensoroSense.DEFAULT_ADDR, SensoroSense.DEFAULT_PORT);
			}
			heartBeatRunnable = new HeartBeatRunnable(heartBeatUtils,
					SensoroSense.DEFAULT_HEARTBEAT_PERIOD, appID, uid);
			heartBeatThread = new Thread(heartBeatRunnable);
			heartBeatThread.start();
		}
	}

	/**
	 * ��ʼ�����ʺ�̨��action���߳�
	 * 
	 * @param
	 * @return
	 * @date 2014��2��18��
	 * @author trs
	 */
	private void initActionThread() {

		if (actionRunnable == null) {

			String log = null;
			if (local_log || remote_log) {
				log = LogUtils.getCurrentLog();
			}
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}
			actionRunnable = new ActionRunnable();
			if (actionThread == null) {
				actionThread = new Thread(actionRunnable);
			}
			actionThread.start();
		}
	}

	/**
	 * ��ʼ�����ʺ�̨��actionͳ�Ƶ��߳�
	 * 
	 * @param
	 * @return
	 * @date 2014��4��17��
	 * @author zwz
	 */
	private void initActionCountThread() {

		if (actionCountThread == null) {

			String log = null;
			if (local_log || remote_log) {
				log = LogUtils.getCurrentLog();
			}
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}
			actionCountRunnable = new ActionCountRunnable();
			if (actionCountThread == null) {
				actionCountThread = new Thread(actionCountRunnable);
			}
			actionCountThread.start();
		}
	}

	/**
	 * ��ʼ�����ʺ�̨��action���߳�
	 * 
	 * @param
	 * @return
	 * @date 2014��2��18��
	 * @author trs
	 */
	private void initBeaconDiscoveryThread() {

		if (beaconDiscoveryRunnable == null) {

			String log = null;
			if (local_log || remote_log) {
				log = LogUtils.getCurrentLog();
			}
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}

			beaconDiscoveryRunnable = new BeaconDiscoveryRunnable();
			if (beaconDiscoveryThread == null) {
				beaconDiscoveryThread = new Thread(beaconDiscoveryRunnable);
				beaconDiscoveryThread.start();
			}

		}
	}

	/**
	 * ��ʼ��Event���߳�
	 * 
	 * @param
	 * @return
	 * @date 2014��4��8��
	 * @author trs
	 */
	private void initEventThread() {
		if (eventRunnable == null) {
			String log = null;
			if (local_log || remote_log) {
				log = LogUtils.getCurrentLog();
			}
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}
			eventRunnable = new EventRunnable();
			if (eventThread == null) {
				eventThread = new Thread(eventRunnable);
				eventThread.start();
			}
		}
	}

	private void initBroadcastReceiver() {
		if (receiver == null) {

			String log = null;
			if (local_log || remote_log) {
				log = LogUtils.getCurrentLog();
			}
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}

			receiver = new BeaconStatusBroadcastReceiver();

			IntentFilter filter = new IntentFilter();
			filter.addAction(BleService.BEACON_BROADCAST_ACTION_NEW);
			filter.addAction(BleService.BEACON_BROADCAST_ACTION_GONE);

			registerReceiver(receiver, filter);
		}
	}

	/**
	 * ��ʼ������
	 * 
	 * @param
	 * @return
	 * @date 2014��3��20��
	 * @author trs
	 */
	private void initUtils() {
		if (configNetUtils == null) {
			configNetUtils = new ConfigNetUtils(this,
					SensoroSense.DEAFULT_SDK_LOG_ADDR,
					SensoroSense.DEFAULT_PORT);
			configNetUtils.setAppID(appID);
		}
	}

	private void initUDPLogThread() {
		if (udpLogRunnable == null) {
			udp = new AndroidUDP(this, SensoroSense.DEAFULT_SDK_LOG_PORT,
					SensoroSense.DEAFULT_SDK_LOG_ADDR, 3000,
					AndroidUDP.SOCKET_MULTIPLY);
			udpLogRunnable = new UdpLogRunnable();
			udpLogthrThread = new Thread(udpLogRunnable);
			udpLogthrThread.start();
		}
	}

	private void initOfflineDB() {
		dataBaseAdpter = new DataBaseAdpter(this);
		dataBaseAdpter.open();
		offileDBUtils = new OfflineDBUtils(dataBaseAdpter);
	}

	/***
	 * �����������ݶ�ʱ��ȡ�߳�
	 */
	private void initOfflineDataThread() {
		sharedPreferences = getSharedPreferences(
				SensoroSense.SENSORO_SENSE_PREF, Context.MODE_PRIVATE);
		// ģ������ʹ��
		// sendOfflineMsg(39.995647, 116.475478, new Date().getTime(), true);

		if (networkAvailableRunnable == null) {
			String log = null;
			if (local_log || remote_log) {
				log = LogUtils.getCurrentLog();
			}
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}
			networkAvailableRunnable = new NetworkAvailableRunnable();
			networkAvailableThread = new Thread(networkAvailableRunnable);
			networkAvailableThread.start();
		}
	}

	/**
	 * �������ģ������,���ģ�������м���Beaconfig����Ϣ
	 * 
	 * @param
	 * @return
	 * @date 2014��3��10��
	 * @author trs
	 */

	private void sendUdpLogMsg(String log) {

		if (udpLoghandler == null) {
			return;
		}
		Message msg = udpLoghandler.obtainMessage(MSG_UDP, log);
		udpLoghandler.sendMessage(msg);
	}

	@Override
	public final IBinder onBind(Intent intent) {

		String log = null;
		if (local_log || remote_log) {
			log = LogUtils.getCurrentLog();
		}
		if (local_log) {
			logger.debug(log);
		}
		if (remote_log) {
			sendUdpLogMsg(log);
		}

		return new FsmServiceBinder();
	}

	/**
	 * ����һ���µ�Beacon
	 * 
	 * @param
	 * @return
	 * @date 2014��3��11��
	 * @author trs
	 */
	abstract public void onNew(Beacon beacon);

	/**
	 * ĳ��Beacon��ʧ
	 * 
	 * @param
	 * @return
	 * @date 2014��3��11��
	 * @author trs
	 */
	abstract public void onGone(Beacon beacon);

	/**
	 * �����
	 * 
	 * @param
	 * @return
	 * @date 2014��3��11��
	 * @author trs
	 */
	abstract public void onEnterSpot(Spot spot, Zone zone);

	/**
	 * �뿪��
	 * 
	 * @param
	 * @return
	 * @date 2014��3��11��
	 * @author trs
	 */
	abstract public void onLeaveSpot(Spot spot, Zone zone);

	/**
	 * �ڵ�ͣ������һֱͣ�������λص������Ϊ��Сͣ��ʱ�䵥λ
	 * 
	 * @param
	 * @return
	 * @date 2014��3��11��
	 * @author trs
	 */
	abstract public void onStaySpot(Spot spot, Zone zone, long seconds);

	/**
	 * ������
	 * 
	 * @param
	 * @return
	 * @date 2014��3��11��
	 * @author trs
	 */
	abstract public void onEnterZone(Zone zone, Spot spot);

	/**
	 * �뿪��
	 * 
	 * @param
	 * @return
	 * @date 2014��3��11��
	 * @author trs
	 */
	abstract public void onLeaveZone(Zone zone, Spot spot);

	/**
	 * ����ͣ������һֱͣ�������λص������Ϊ��Сͣ��ʱ�䵥λ
	 * 
	 * @param
	 * @return
	 * @date 2014��3��11��
	 * @author trs
	 */
	abstract public void onStayZone(Zone zone, Spot spot, long seconds);

	/**
	 * action�ص�����
	 * 
	 * @param
	 * @return
	 * @date 2014��3��11��
	 * @author trs
	 */
	abstract public void onAction(Action action);

	class FsmServiceBinder extends IFsmService.Stub {
		FsmService getService() {
			return FsmService.this;
		}

		@Override
		public Spot[] getSpot() throws RemoteException {
			ArrayList<Spot> spots = FsmService.this.getSpot();
			return (Spot[]) spots.toArray(new Spot[(spots.size())]);
		}

		@Override
		public Zone[] getZones() throws RemoteException {

			ArrayList<Zone> zones = FsmService.this.getZones();
			return (Zone[]) zones.toArray(new Zone[(zones.size())]);
		}

		@Override
		public void setUID(String uid) throws RemoteException {
			FsmService.this.bindUID(uid);
		}

		@Override
		public void initHeartBeat() throws RemoteException {
			FsmService.this.initHeartBeat();
		}

		@Override
		public void setUUID(String uuid) throws RemoteException {
			FsmService.this.bindUUID(uuid);
		}
	}

	/**
	 * ����BleEvent���߳�
	 * 
	 * @param
	 * @return
	 * @date 2014��3��11��
	 * @author trs
	 */
	class BleEventRunnable implements Runnable {

		@Override
		public void run() {
			Looper.prepare();
			bleEventHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					int what = msg.what;
					BleEvent bleEvent = null;
					switch (what) {
					case BLE_EVENT_MSG_NEW:
						bleEvent = (BleEvent) msg.obj;
						newBleEvent(bleEvent);
						break;
					case BLE_EVENT_MSG_GONE:
						bleEvent = (BleEvent) msg.obj;
						goneBleEvent(bleEvent);
						break;
					default:
						break;
					}
					super.handleMessage(msg);
				}
			};
			Looper.loop();
		}

		/**
		 * �����µ�Beacon
		 * 
		 * @param
		 * @return
		 * @date 2014��3��11��
		 * @author trs
		 */
		private void newBleEvent(BleEvent bleEvent) {

			if (bleEvent == null) {
				return;
			}
			String log = null;
			if (local_log || remote_log) {
				log = LogUtils.getCurrentLog("newBleEvent : " + bleEvent.id);
			}
			if (local_log) {
				logger.debug(log);
			}
			if (remote_log) {
				sendUdpLogMsg(log);
			}
			// get beaconConfig
			BeaconConfig beaconConfig = null;
			ArrayList<ZoneConfig> zoneConfigs = null;
			try {
				// ��ȡBeaconConfig֮ǰ�жϸ�BleEvent�Ƿ��ڻ�ȡ����BeaconConfig��������
				BleEvent tmpbleEvent = cache
						.getNoBeaconConfigBleEvent(bleEvent.id);
				if (tmpbleEvent != null) {
					return;
				}
				beaconConfig = getBeaconConfig(bleEvent);
				// ���beaconConfigs�ǿյ�,���������beaconConfigs����,30���Ӳ���ʹ�ø�bleEvent��ȡbeaconConfigs
				if (beaconConfig == null) {
					// ������beaconConfigs����
					cacheNoBeaconConfigBleEvent(bleEvent);
					return;
				} else {
					// �����beacon����ĳ��zone,���ȡZoneConfig
					if (beaconConfig.zids != null
							&& beaconConfig.zids.size() != 0) {
						// ��ȡZoneConfig
						zoneConfigs = getZoneConfigs(beaconConfig);
						if (zoneConfigs == null) {
							// ����zid�����ȡ����ZoneConfig��������
							cache.putNoZoneConfigIDs(beaconConfig.zids);
						}
					}
				}
			} catch (SensoroException e) {
				String errInfoString = e.getMessage();
				loggerInfo(errInfoString);
				return;
			}

			if (zoneConfigs != null && zoneConfigs.size() != 0) {
				// ��beacon����ĳ������
				// ����BeaconConfig����Fsm,zid�ж�����½����״̬��
				for (ZoneConfig unit : zoneConfigs) {
					doConfig2Fsm(beaconConfig, unit, cache, bleEvent,
							bleEvent.time);
				}
			} else {
				// ��beacon�������κ�һ������
				doConfig2Fsm(beaconConfig, null, cache, bleEvent, bleEvent.time);
			}
		}

		/**
		 * ����beacon��Χ(beacon��ʧ��)
		 * 
		 * @param
		 * @return
		 * @date 2014��3��11��
		 * @author trs
		 */
		private void goneBleEvent(BleEvent bleEvent) {
			if (bleEvent == null) {
				return;
			}
			long time = bleEvent.time;
			// get beaconConfig
			BeaconConfig beaconConfig = null;
			ArrayList<ZoneConfig> zoneConfigs = null;
			try {
				// ��ȡBeaconConfig֮ǰ�жϸ�BleEvent�Ƿ��ڻ�ȡ����BeaconConfig��������
				BleEvent tmpbleEvent = cache
						.getNoBeaconConfigBleEvent(bleEvent.id);
				if (tmpbleEvent != null) {
					return;
				}
				beaconConfig = getBeaconConfig(bleEvent);
				// ���beaconConfigs�ǿյ�,���������beaconConfigs����,30���Ӳ���ʹ�ø�bleEvent��ȡbeaconConfigs
				if (beaconConfig == null) {
					// ������beaconConfigs����
					cacheNoBeaconConfigBleEvent(bleEvent);
					return;
				} else {
					// �����beacon����ĳ��zone,���ȡZoneConfig
					if (beaconConfig.zids != null) {
						// ��ȡZoneConfig
						zoneConfigs = getZoneConfigs(beaconConfig);
						if (zoneConfigs != null) {
							for (ZoneConfig unit : zoneConfigs) {
								cache.putZoneConfig(unit.id, unit);
							}
						} else {
							// ����zid�����ȡ����ZoneConfig��������
							cache.putNoZoneConfigIDs(beaconConfig.zids);
						}
					}
				}
			} catch (SensoroException e) {
				String errInfoString = e.getMessage();
				loggerInfo(errInfoString);
				return;
			}

			if (zoneConfigs != null && zoneConfigs.size() != 0) {
				// ��״̬����������ZONE
				for (ZoneConfig unit : zoneConfigs) {
					doConfigGone2Fsm(beaconConfig, unit, cache, bleEvent, time);
				}
			} else {
				// ��״̬����������beacon
				doConfigGone2Fsm(beaconConfig, null, cache, bleEvent, time);
			}
		}
	}

	/**
	 * �����ȡ����BeaconConfig��BleEvent
	 * 
	 * @param
	 * @return
	 * @date 2014��4��16��
	 * @author trs
	 */
	private void cacheNoBeaconConfigBleEvent(BleEvent bleEvent) {

		// ������beaconConfigs����
		cache.putNoBeaconConfigBleEvent(bleEvent.id, bleEvent);
		if (DEBUG) {
			Log.v(TAG, "goneBleEvent,������config beacon--major" + bleEvent.major
					+ "minor" + bleEvent.minor);
		}
		String log = null;
		if (local_log || remote_log) {
			log = LogUtils.getCurrentLog("get beaconConfig null : "
					+ bleEvent.id);
		}
		if (local_log) {
			logger.debug(log);
		}
		if (remote_log) {
			sendUdpLogMsg(log);
		}
	}

	private void beaconConfigGoneTrigger(Fsm fsm, BeaconConfig beaconConfig,
			ZoneConfig zoneConfig, long time) {

		if (fsm == null) {
			return;
		}
		BeaconConfig cacheBeaconConfig = null;

		// �����뿪�����Ϣ,����û���жϸ�beaconConfig�Ƿ������zone����,�߼���Ӧ�ò����ж�
		sendEventMsg(zoneConfig, beaconConfig, MSG_LEAVE_SPOT, -1);
		// ��״̬����Beacon Map��ɾ����Ӧ��BeaconConfig
		cacheBeaconConfig = fsm.beaconConfigMap.remove(beaconConfig._id);
		// ɾ����ӦBeacon��ͣ������
		fsm.spotStayTimesHashMap.remove(beaconConfig._id);
		// ɾ����ӦBeacon��ͣ��ʱ��
		fsm.inSpotTimeHashMap.remove(beaconConfig._id);
		// ���״̬����������Beacon
		if (fsm.levelinner == Fsm.LEVEL_BEACON) {
			if (fsm.beaconConfigMap.size() <= 0) {
				// �����Beacon Fsm��beacon����С�ڵ���0,�߼���Beacon Fsmֻ��һ��Beacon,����жϿ��Բ���
				cache.removeFsm(beaconConfig._id);
			}
		}
		// ����action��Ϣ
		actCtrlAction(cacheBeaconConfig, zoneConfig, MSG_LEAVE_SPOT_ACTION, -1);
		if (fsm.beaconConfigMap.size() <= 0 && fsm.levelinner == Fsm.LEVEL_ZONE) {
			// ��״̬���е�map��û���κ�һ��beacon,���Ҹ�״̬����������zone,���ʾ����zone
			fsm.spotStayTimesHashMap.clear();
			fsm.inSpotTimeHashMap.clear();
			fsm.zoneStayTimes = 0;
			fsm.inZone = false;
			cache.removeFsm(zoneConfig.id);
			sendEventMsg(zoneConfig, cacheBeaconConfig, MSG_LEAVE_ZONE, -1);
			// ����action��Ϣ
			actCtrlAction(cacheBeaconConfig, zoneConfig, MSG_LEAVE_ZONE_ACTION,
					-1);
		}
	}

	/**
	 * ״̬��״̬�����,���ฺ�����ͣ��ʱ�������Ƶ��¼�
	 * 
	 * @param
	 * @return
	 * @date 2014��3��18��
	 * @author trs
	 */
	class FsmStatusRunnable implements Runnable {

		@Override
		public void run() {

			while (true) {
				if (stopFlag) {
					break;
				}
				Collection<Fsm> fsms = cache.fsmMap.values();
				loggerInfo("cache.fsmMap size = " + cache.fsmMap.size());
				long time = new Date().getTime();
				for (Fsm fsm : fsms) {
					doTimeOutEvent(fsm, time);
				}

				try {
					Thread.sleep(SensoroSense.DEFAULT_FSM_STATUS_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		BeaconConfig getFsmLastBeaconConfig(Fsm fsm) {
			if (fsm == null || fsm.beaconConfigMap == null
					|| fsm.beaconConfigMap.size() == 0) {
				return null;
			}
			Iterator<Entry<String, BeaconConfig>> iterator = fsm.beaconConfigMap
					.entrySet().iterator();
			BeaconConfig lastBeaconConfig = null;
			while (iterator.hasNext()) {
				Map.Entry<String, BeaconConfig> entry = (Map.Entry<String, BeaconConfig>) iterator
						.next();
				lastBeaconConfig = entry.getValue();
			}
			return lastBeaconConfig;
		}

		/**
		 * ����stay�¼�
		 * 
		 * @param
		 * @return
		 * @date 2014��3��19��
		 * @author trs
		 */
		private void doTimeOutEvent(Fsm fsm, long time) {

			long timeOut = 0;
			long space = 0;

			if (fsm.zoneConfig != null) {
				// �����������ô���stay��action
				// ��״̬���������״̬��
				// �ж�zone��ͣ��ʱ��
				space = time - fsm.inZoneTime;
				timeOut = beaconSet.zoneTimer * (fsm.zoneStayTimes + 1);
				// ��ȡ��ǰ���ڵ�beaconconfig
				if (fsm.beaconConfigMap == null
						|| fsm.beaconConfigMap.size() == 0) {
					return;
				}
				// ��ȡ���һ��BeaconConfig,ֻ�����һ��beacon���津��ͣ���¼�
				BeaconConfig lastBeaconConfig = getFsmLastBeaconConfig(fsm);
				if (lastBeaconConfig != null) {
					if (space >= timeOut) {

						sendEventMsg(fsm.zoneConfig, lastBeaconConfig,
								MSG_STAY_ZONE, timeOut);
						actCtrlAction(lastBeaconConfig, fsm.zoneConfig,
								MSG_STAY_ZONE_ACTION, timeOut);
						fsm.zoneStayTimes++;
					}
				}
			}

			// ����spot��stay�¼�
			if (fsm.beaconConfigMap.size() == 0) {
				// ���beaconConfigMap��û��BeaconConfig,�򲻴���
			} else {
				Collection<BeaconConfig> beaconConfigs = fsm.beaconConfigMap
						.values();
				for (BeaconConfig beaconConfig : beaconConfigs) {
					if (beaconConfig != null) {
						// �ж�spot��ͣ��ʱ��
						int spotStayTimes = 0;
						Integer temp = fsm.spotStayTimesHashMap
								.get(beaconConfig._id);
						if (temp != null) {
							spotStayTimes = temp.intValue();
						}
						timeOut = beaconSet.spotTimer * (spotStayTimes + 1);
						Long inSpotTime = fsm.inSpotTimeHashMap
								.get(beaconConfig._id);
						if (inSpotTime == null) {
							// Ϊ0���ʾ,��û�н������beacon,��һ�ִ����״̬,�����ϲ����������״̬
							continue;
						}
						space = time - inSpotTime;
						// ��ȡ��ǰ���ڵ�beaconconfig
						if (fsm.zoneConfig == null) {
							return;
						}
						if (space >= timeOut) {
							sendEventMsg(fsm.zoneConfig, beaconConfig,
									MSG_STAY_SPOT, timeOut);
							spotStayTimes++;
							fsm.spotStayTimesHashMap.put(beaconConfig._id,
									spotStayTimes);
							actCtrlAction(beaconConfig, fsm.zoneConfig,
									MSG_STAY_SPOT_ACTION, timeOut);
						}
					}

				}
			}

		}
	}

	/**
	 * �������BleEvent�㲥,beacon�ĳ��ֺ���ʧ��������ִ��
	 * 
	 * @param
	 * @return
	 * @date 2014��3��11��
	 * @author trs
	 */
	class BeaconStatusBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isNoBeaconConfigBleEvent = false;
			String action = intent.getAction();
			BleEvent bleEvent = null;
			bleEvent = intent.getParcelableExtra(BleService.BLE_EVENT);
			// �����µ�beacon
			if (bleEventHandler == null || bleEvent == null) {
				return;
			}
			// �жϸ�bleEvent�Ƿ��ڻ�ȡ����BeaconConfig��������,���������Ч���ڲ��������bleEvents����
			BleEvent noConfigBleEvent = cache
					.getNoBeaconConfigBleEvent(bleEvent.id);
			if (noConfigBleEvent != null) {
				if (DEBUG) {
					Log.v(TAG, BleService.BEACON_BROADCAST_ACTION_NEW
							+ "û��config beacon---major" + bleEvent.major
							+ "minor" + bleEvent.minor);
				}
				isNoBeaconConfigBleEvent = true;
			}
			if (action.equals(BleService.BEACON_BROADCAST_ACTION_NEW)) {
				// HW�㷢���µ�beacon,����onNew�Ļص�
				sendDiscoveryMsg(bleEvent, true);

				if (!isNoBeaconConfigBleEvent) {
					// ���ͷ����µ�beacon����Ϣ
					String log = null;
					if (local_log || remote_log) {
						log = LogUtils.getCurrentLog("new beacon");
					}
					if (local_log) {
						logger.debug(log);
					}
					if (remote_log) {
						sendUdpLogMsg(log);
					}

					Message msg = bleEventHandler.obtainMessage(
							BLE_EVENT_MSG_NEW, bleEvent);
					bleEventHandler.sendMessage(msg);
				}
			} else if (action.equals(BleService.BEACON_BROADCAST_ACTION_GONE)) {
				// HW�㷢���µ�beacon,����onGone�Ļص�
				sendDiscoveryMsg(bleEvent, false);

				if (!isNoBeaconConfigBleEvent) {
					// beacon��ʧ
					String log = null;
					if (local_log || remote_log) {
						log = LogUtils.getCurrentLog("gone beacon");
					}
					if (local_log) {
						logger.debug(log);
					}
					if (remote_log) {
						sendUdpLogMsg(log);
					}

					Message msg = bleEventHandler.obtainMessage(
							BLE_EVENT_MSG_GONE, bleEvent);
					bleEventHandler.sendMessage(msg);
				}
			}
		}

	}

	class HeartBeatRunnable implements Runnable {
		private BeaconHeartBeatUtils heartBeatUtils;
		private long heartBeatPeriod;
		private String appID;
		private String uid;

		public HeartBeatRunnable(BeaconHeartBeatUtils heartBeatUtils,
				long heartBeatPeriod, String appID, String uid) {
			super();
			this.heartBeatUtils = heartBeatUtils;
			this.heartBeatPeriod = heartBeatPeriod;
			this.appID = appID;
			this.uid = uid;
		}

		@Override
		public void run() {
			if (heartBeatUtils == null || heartBeatThread == null
					|| appID == null) {
				return;
			}
			while (true) {
				if (heartBeatStopFlag) {
					break;
				}
				try {
					heartBeatUtils.HeartBeat(this.uid, this.appID);
				} catch (SensoroException e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(heartBeatPeriod);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	}

	class ActionCountRunnable implements Runnable {

		@Override
		public void run() {
			Looper.prepare();
			actionCountHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					int what = msg.what;
					Bundle bundle = msg.getData();
					ZoneConfig zoneConfig = (ZoneConfig) bundle
							.get(ZONE_CONFIG);
					BeaconConfig beaconConfig = (BeaconConfig) bundle
							.get(BEACON_CONFIG);
					long seconds = bundle.getLong(SECONDS);
					netAction(zoneConfig, beaconConfig, what, seconds, false);
					super.handleMessage(msg);
				}
			};
			Looper.loop();
		}

	}

	// ����action��Runnable
	class ActionRunnable implements Runnable {

		@Override
		public void run() {
			Looper.prepare();
			actionHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					int what = msg.what;
					boolean remote = true; // Ĭ�ϴ������ȡaction�߼�

					Bundle bundle = msg.getData();

					ZoneConfig zoneConfig = (ZoneConfig) bundle
							.get(ZONE_CONFIG);
					BeaconConfig beaconConfig = (BeaconConfig) bundle
							.get(BEACON_CONFIG);
					long seconds = bundle.getLong(SECONDS);
					Act beaconConfigAct = beaconConfig.act;
					Act zoneConfigAct = null;
					if (zoneConfig != null) {
						zoneConfigAct = zoneConfig.act;
					}
					ActEvent actEvent = null;
					switch (what) {
					case MSG_ENTER_SPOT_ACTION:

						if (beaconConfigAct == null) {
							remote = true;
						} else {
							actEvent = beaconConfigAct.enter;
							if (actEvent != null) {
								remote = actEvent.remote;
							} else {
								remote = true;
							}
						}
						if (remote) {
							netAction(zoneConfig, beaconConfig, what, seconds,
									remote);
						} else {
							localTrigger(zoneConfig, beaconConfig, what,
									seconds);
						}
						break;
					case MSG_LEAVE_SPOT_ACTION:

						if (beaconConfigAct == null) {
							remote = true;
						} else {
							actEvent = beaconConfigAct.leave;
							if (actEvent != null) {
								remote = actEvent.remote;
							} else {
								remote = true;
							}
						}
						if (remote) {
							netAction(zoneConfig, beaconConfig, what, seconds,
									remote);
						} else {
							localTrigger(zoneConfig, beaconConfig, what,
									seconds);
						}

						break;
					case MSG_STAY_SPOT_ACTION:

						if (beaconConfigAct == null) {
							remote = true;
						} else {
							actEvent = beaconConfigAct.stay;
							if (actEvent != null) {
								remote = actEvent.remote;
							} else {
								remote = true;
							}
						}
						if (remote) {
							netAction(zoneConfig, beaconConfig, what, seconds,
									remote);
						} else {
							localTrigger(zoneConfig, beaconConfig, what,
									seconds);
						}

						break;
					case MSG_ENTER_ZONE_ACTION:
						if (zoneConfigAct == null) {
							remote = true;
						} else {
							actEvent = zoneConfigAct.enter;
							if (actEvent != null) {
								remote = actEvent.remote;
							} else {
								remote = true;
							}
						}
						if (remote) {
							netAction(zoneConfig, beaconConfig, what, seconds,
									remote);
						} else {
							localTrigger(zoneConfig, beaconConfig, what,
									seconds);
						}
						break;

					case MSG_LEAVE_ZONE_ACTION:
						if (zoneConfigAct == null) {
							remote = true;
						} else {
							actEvent = zoneConfigAct.leave;
							if (actEvent != null) {
								remote = actEvent.remote;
							} else {
								remote = true;
							}
						}
						if (remote) {
							netAction(zoneConfig, beaconConfig, what, seconds,
									remote);
						} else {
							localTrigger(zoneConfig, beaconConfig, what,
									seconds);
						}
						break;

					case MSG_STAY_ZONE_ACTION:
						if (zoneConfigAct == null) {
							remote = true;
						} else {
							actEvent = zoneConfigAct.stay;
							if (actEvent != null) {
								remote = actEvent.remote;
							} else {
								remote = true;
							}
						}
						if (remote) {
							netAction(zoneConfig, beaconConfig, what, seconds,
									remote);
						} else {
							localTrigger(zoneConfig, beaconConfig, what,
									seconds);
						}
						break;

					default:
						break;
					}
					super.handleMessage(msg);
				}
			};
			Looper.loop();
		}

		/**
		 * ���ش���action�ص�,�������۴ӱ��ػ��ǲ����紥��sdk�ص�,��Ҫ�����������,��������Ϊ������ͳ�Ƶ���Ҫ
		 * 
		 * @param
		 * @return
		 * @date 2014��4��16��
		 * @author trs
		 */
		private boolean localTrigger(ZoneConfig zoneConfig,
				BeaconConfig beaconConfig, int msgID, long seconds) {

			// ��ȡ��Ӧaction֮ǰ��������Ϣ
			if (dataBaseAdpter == null) {
				return false;
			}
			// ��ȡtype��name,type����zone��spot,name����enter,leave,stay
			// ��ȡid,�����zone��ʹ��zid,�����spotʹ��beacon.id,����������id����app�Լ������
			int type = -1;
			int name = -1;
			String id = null;
			ActEvent actEvent = null;

			switch (msgID) {
			case MSG_ENTER_ZONE_ACTION:
				type = DataBaseAdpter.TYPE_ZONE;
				name = DataBaseAdpter.NAME_ENTER;
				if (zoneConfig != null) {
					id = zoneConfig.id;
				}
				if (zoneConfig.act != null) {
					actEvent = zoneConfig.act.enter;
				}

				break;
			case MSG_LEAVE_ZONE_ACTION:
				type = DataBaseAdpter.TYPE_ZONE;
				name = DataBaseAdpter.NAME_LEAVE;
				if (zoneConfig != null) {
					id = zoneConfig.id;
				}
				if (zoneConfig.act != null) {
					actEvent = zoneConfig.act.leave;
				}

				break;
			case MSG_STAY_ZONE_ACTION:
				type = DataBaseAdpter.TYPE_ZONE;
				name = DataBaseAdpter.NAME_STAY;
				if (zoneConfig != null) {
					id = zoneConfig.id;
				}
				if (zoneConfig.act != null) {
					actEvent = zoneConfig.act.stay;
				}
				break;
			case MSG_ENTER_SPOT_ACTION:
				type = DataBaseAdpter.TYPE_SPOT;
				name = DataBaseAdpter.NAME_ENTER;
				if (beaconConfig != null) {
					id = beaconConfig._id;
				}
				if (beaconConfig.act != null) {
					actEvent = beaconConfig.act.enter;
				}
				break;
			case MSG_LEAVE_SPOT_ACTION:
				type = DataBaseAdpter.TYPE_SPOT;
				name = DataBaseAdpter.NAME_LEAVE;
				if (beaconConfig != null) {
					id = beaconConfig._id;
				}
				if (beaconConfig.act != null) {
					actEvent = beaconConfig.act.leave;
				}
				break;
			case MSG_STAY_SPOT_ACTION:
				type = DataBaseAdpter.TYPE_SPOT;
				name = DataBaseAdpter.NAME_STAY;
				if (beaconConfig != null) {
					id = beaconConfig._id;
				}
				if (beaconConfig.act != null) {
					actEvent = beaconConfig.act.stay;
				}
				break;

			default:
				break;
			}
			localAction(type, name, id, actEvent, beaconConfig, zoneConfig,
					msgID);
			return true;
		}

		private void localAction(int type, int name, String id,
				ActEvent actEvent, BeaconConfig beaconConfig,
				ZoneConfig zoneConfig, int msgID) {
			if (id == null) {
				loggerInfo("localAction id is null");
				return;
			}
			ActionRecord actionRecord = null;
			try {
				actionRecord = offileDBUtils.getActionRecord(id, type, name);

				Action action = null;
				boolean res = false;
				if (actionRecord == null) {
					// û�в�ѯ��actionRecord.��˵��֮ǰû�д�������action,�ж�act���߼�,���ҽ���action�������ݿ�
					// �ж�ÿ�촥����Ƶ��
					if (actEvent.freq.daily > 0) {
						// ÿ�촥����Ƶ���������0,����action
						action = getAction(type, name, actEvent, beaconConfig,
								zoneConfig);
						onAction(action);
						// ������ݿ�
						res = offileDBUtils.addActionRecord(id, type, name,
								new Date().getTime(), 1);
						if (!res) {
							// �������ݿ�ʧ��
							loggerInfo("addActionRecord err");
						}
					}
				} else {
					// ��ѯ����Ӧ��actionRecord,�����Ƶ��ҵ���߼��ж�,ÿ���Ƶ�ο��ƺ�ʱ�����Ŀ��Ʋ�ͬʱ����

					// �������Ƶ�ο��ƶ���-1.�������ƴ���action
					Date now = new Date();
					DateBrief lastDate = DateUtils
							.getDate(actionRecord.timestamp);
					DateBrief date = DateUtils.getDate(now);

					if (actEvent.freq.daily == -1
							&& actEvent.freq.interval == -1) {
						action = getAction(type, name, actEvent, beaconConfig,
								zoneConfig);
						onAction(action);
						// //���ݿ����action��������
						int result = offileDBUtils.updateActionRecord(id, type,
								name, new Date().getTime(),
								actionRecord.times++);
						loggerInfo("updateActionRecord result -- " + result);
						sendActionCountMsg(beaconConfig, zoneConfig, msgID, -1);
					} else if (actEvent.freq.daily != -1) {
						// ����ÿ���Ƶ���ж�,dailyΪ-1��ʾû��ÿ���Ƶ�ο���
						// ������ڲ���ͬһ��,�����¼���Ƶ��
						if (lastDate.year == date.year
								&& lastDate.day == date.day
								&& lastDate.month == date.month) {
							// �����ͬһ��,�Ƚ�Ƶ��
							if (actionRecord.times >= actEvent.freq.daily) {
								// ���ݿ��¼action�Ĵ����������ڻ����ÿ��Ĵ���Ƶ��,�򲻴���action
							} else {
								// ���ݿ����action��������
								int result = offileDBUtils.updateActionRecord(
										id, type, name, new Date().getTime(),
										actionRecord.times + 1);
								loggerInfo("updateActionRecord result -- "
										+ result);

								action = getAction(type, name, actEvent,
										beaconConfig, zoneConfig);
								onAction(action);
								sendActionCountMsg(beaconConfig, zoneConfig,
										msgID, -1);
							}
						} else {
							// ������ڲ���ͬһ��,�����¼���Ƶ��
							// ������Ƶ���ó�1
							int result = offileDBUtils.updateActionRecord(id,
									type, name, now.getTime(), 1);
							loggerInfo("updateActionRecord result -- " + result);

							action = getAction(type, name, actEvent,
									beaconConfig, zoneConfig);
							onAction(action);
							sendActionCountMsg(beaconConfig, zoneConfig, msgID,
									-1);
						}

					} else if (actEvent.freq.interval != -1) {
						// ����ʱ������Ƶ���ж�,���intervalΪ-1��ʾû��ʱ����Ƶ�ο���
						now = new Date();
						long space = now.getTime() - actionRecord.timestamp;
						if (space > actEvent.freq.interval) {
							// ���ʱ��������interval�򴥷�
							action = getAction(type, name, actEvent,
									beaconConfig, zoneConfig);
							onAction(action);
							// �������ݿ�
							int curTimes = actionRecord.times + 1;
							int result = offileDBUtils.updateActionRecord(id,
									type, name, now.getTime(), curTimes);
							loggerInfo("updateActionRecord result -- " + result);
							sendActionCountMsg(beaconConfig, zoneConfig, msgID,
									-1);
						}
					}
				}
			} catch (SensoroException e) {
				e.printStackTrace();
			}

		}

		private Action getAction(int type, int name, ActEvent actEvent,
				BeaconConfig beaconConfig, ZoneConfig zoneConfig) {

			Action action = new Action();

			action.param = actEvent.param;
			action.spot = ClassTrans.beaconConfig2Spot(beaconConfig);
			action.zone = ClassTrans.zoneConfigtoZone(zoneConfig);
			action.action = getActionName(type, name);

			return action;
		}

		private String getActionName(int type, int name) {

			String action = null;

			if (type == DataBaseAdpter.TYPE_SPOT) {
				switch (name) {
				case DataBaseAdpter.NAME_ENTER:
					action = SensoroSense.ACTION_ENTER_SPOT;
					break;
				case DataBaseAdpter.NAME_LEAVE:
					action = SensoroSense.ACTION_LEAVE_SPOT;
					break;
				case DataBaseAdpter.NAME_STAY:
					action = SensoroSense.ACTION_STAY_SPOT;
					break;

				default:
					break;
				}
			} else if (type == DataBaseAdpter.TYPE_ZONE) {

				switch (name) {
				case DataBaseAdpter.NAME_ENTER:
					action = SensoroSense.ACTION_ENTER_ZONE;
					break;
				case DataBaseAdpter.NAME_LEAVE:
					action = SensoroSense.ACTION_LEAVE_ZONE;
					break;
				case DataBaseAdpter.NAME_STAY:
					action = SensoroSense.ACTION_STAY_ZONE;
					break;

				default:
					break;
				}
			}
			return action;
		}
	}

	private String netActionConnect(ZoneConfig zoneConfig,
			BeaconConfig beaconConfig, int msgID, long seconds, String uid) {

		if (netUtils == null) {
			// ��������sdk�����Ͳ��Ի���
			netUtils = new ActionNetUtils(FsmService.this, uuid,
					SensoroSense.DEFAULT_ADDR, SensoroSense.DEFAULT_PORT);
		}
		String result = null;
		long nanoTime = System.nanoTime();
		if (msgID == MSG_ENTER_SPOT_ACTION) {
			result = netUtils.spotAction(appID, ActionNetUtils.ENTER, -1,
					zoneConfig, beaconConfig, uid, uuid);
		} else if (msgID == MSG_LEAVE_SPOT_ACTION) {
			result = netUtils.spotAction(appID, ActionNetUtils.LEAVE, -1,
					zoneConfig, beaconConfig, uid, uuid);
		} else if (msgID == MSG_STAY_SPOT_ACTION) {
			result = netUtils.spotAction(appID, ActionNetUtils.STAY, seconds,
					zoneConfig, beaconConfig, uid, uuid);
		} else if (msgID == MSG_ENTER_ZONE_ACTION) {
			result = netUtils.zoneAction(appID, ActionNetUtils.ENTER, -1,
					zoneConfig, beaconConfig, uid, uuid);
		} else if (msgID == MSG_LEAVE_ZONE_ACTION) {
			result = netUtils.zoneAction(appID, ActionNetUtils.LEAVE, -1,
					zoneConfig, beaconConfig, uid, uuid);
		} else if (msgID == MSG_STAY_ZONE_ACTION) {
			result = netUtils.zoneAction(appID, ActionNetUtils.STAY, seconds,
					zoneConfig, beaconConfig, uid, uuid);
		}
		String log = null;
		if (local_log || remote_log) {
			log = LogUtils.getCurrentLog("net comunicate time : "
					+ (System.nanoTime() - nanoTime));
		}
		if (local_log) {
			logger.debug(log);
		}
		if (remote_log) {
			sendUdpLogMsg(log);
		}

		return result;
	}

	private void netAction(ZoneConfig zoneConfig, BeaconConfig beaconConfig,
			int msgID, long seconds, boolean remote) {

		String result = null;
		ActionBrief actionBrief = null;
		Action action = null;

		result = netActionConnect(zoneConfig, beaconConfig, msgID, seconds, uid);
		if (remote) {
			try {
				actionBrief = BeaconJsonUtils.parseBriefAction(result);
				if (actionBrief != null) {
					// ��actionBrief ת����action
					action = ClassTrans.actionBrief2Action(actionBrief, cache,
							configNetUtils);
					onAction(action);
				} else {
					Log.v(TAG, "action is null,what = " + msgID);
				}
			} catch (SensoroException e) {
				return;
			}
		}
	}

	// beacon���ֺ���ʧ��Runnable
	class BeaconDiscoveryRunnable implements Runnable {

		@Override
		public void run() {
			Looper.prepare();
			beaconDiscoveryHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					int what = msg.what;
					BleEvent bleEvent = (BleEvent) msg.obj;
					Beacon beacon = null;

					switch (what) {
					case MSG_NEW_BEACON:
						// ����һ��Beacon�Ļص�
						beacon = BleEvent.toBeacon(bleEvent);
						onNew(beacon);
						break;

					case MSG_GONE_BEACON:
						// ��ʧһ��Beacon�Ļص�
						beacon = BleEvent.toBeacon(bleEvent);
						onGone(beacon);
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

	// Event������Runnable
	class EventRunnable implements Runnable {

		@Override
		public void run() {
			Looper.prepare();
			eventHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					int what = msg.what;
					Bundle bundle = null;
					Zone zone = null;
					Spot spot = null;
					long seconds = 0;
					switch (what) {

					case MSG_ENTER_ZONE:
						bundle = msg.getData();
						if (bundle == null) {
							return;
						}
						zone = (Zone) bundle.get(ZONE);
						spot = (Spot) bundle.get(SPOT);
						onEnterZone(zone, spot);
						break;
					case MSG_LEAVE_ZONE:
						bundle = msg.getData();
						if (bundle == null) {
							return;
						}
						zone = (Zone) bundle.get(ZONE);
						spot = (Spot) bundle.get(SPOT);
						onLeaveZone(zone, spot);
						break;
					case MSG_STAY_ZONE:
						bundle = msg.getData();
						if (bundle == null) {
							return;
						}
						zone = (Zone) bundle.get(ZONE);
						spot = (Spot) bundle.get(SPOT);
						seconds = bundle.getLong(SECONDS);
						onStayZone(zone, spot, seconds);
						break;
					case MSG_ENTER_SPOT:
						bundle = msg.getData();
						if (bundle == null) {
							return;
						}
						zone = (Zone) bundle.get(ZONE);
						spot = (Spot) bundle.get(SPOT);
						onEnterSpot(spot, zone);
						break;
					case MSG_LEAVE_SPOT:
						bundle = msg.getData();
						if (bundle == null) {
							return;
						}
						zone = (Zone) bundle.get(ZONE);
						spot = (Spot) bundle.get(SPOT);
						onLeaveSpot(spot, zone);
						break;
					case MSG_STAY_SPOT:
						bundle = msg.getData();
						if (bundle == null) {
							return;
						}
						zone = (Zone) bundle.get(ZONE);
						spot = (Spot) bundle.get(SPOT);
						seconds = bundle.getLong(SECONDS);
						onStaySpot(spot, zone, seconds);
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

	class UdpLogRunnable implements Runnable {

		@Override
		public void run() {
			Looper.prepare();

			udpLoghandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					int what = msg.what;
					String log = (String) msg.obj;
					if (log == null || udp == null) {
						return;
					}
					switch (what) {
					case MSG_UDP:
						LogUtils.updLog(udp, log);
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

	class LocationRunnable implements Runnable {

		@Override
		public void run() {
			Looper.prepare();

			locationHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					int what = msg.what;
					switch (what) {
					case MSG_LOCATE:
						Bundle bundle = msg.getData();
						double lat = bundle.getDouble(LAT);
						double lon = bundle.getDouble(LON);
						long time = bundle.getLong(TIMESTAMP);
						String result = configNetUtils.getOfflineBeaconConfigs(
								lat, lon, time);
						if (result != null) {
							try {
								boolean res = OfflineJsonDBUtils.saveConfigs(
										result, dataBaseAdpter);
								if (res) {
									// ����洢�ɹ�,��¼�洢�ɹ�ʱ��
									sharedPreferences
											.edit()
											.putLong(
													SensoroSense.LAST_OFFLINE_DATA_TIME,
													System.currentTimeMillis())
											.commit();
								}
							} catch (SensoroException e) {
								e.printStackTrace();
							}
						} else {
							logger.info("get offline beacon failed");
						}
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

	/**
	 * ����δ����״̬��,ÿ�������ж������Ƿ����ӳɹ� ���ɹ��������,�ɹ����ȡ��������
	 * 
	 * @param
	 * @return
	 * @date 2014��3��18��
	 * @author trs
	 */
	class NetworkAvailableRunnable implements Runnable {

		@Override
		public void run() {
			while (true) {

				// �ж������Ƿ�����,������������������ͨ��,��ȡ��������
				boolean isNetValid = isNetworkConnected(FsmService.this);
				if (isNetValid) {
					Long offlineTime = sharedPreferences.getLong(
							SensoroSense.LAST_OFFLINE_DATA_TIME, -1);

					// ��ȡ�Ƿ����ɵ���λ�ñ仯����Ļ�ȡ������������
					boolean isLocationToGet = sharedPreferences.getBoolean(
							SensoroSense.IS_LOCATION_TO_GET, false);
					// ��������ڵ���λ�ú�ʱ��ı仯�����
					if (isLocationToGet
							|| (offlineTime > 0 && offlineTime < System
									.currentTimeMillis()
									+ SensoroSense.GET_OFFLINE_PERIOD)) {
						// ִ���������,�ж��Ƿ���Ҫ����������������
						sendOfflineMsg(lastLocationLat, lastLocationLon,
								lastLocationTime, isLocationToGet);
					}
				}

				try {
					Thread.sleep(SensoroSense.JUDGE_NETWORK_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}

	class OfflineLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location location) {

			// �洢��λ����Ϣ,�����ǰ���粻ͨ���߻�ȡ��������ʧ��,�ṩ���´������ȡ��ʾ����ʹ��
			lastLocationLat = location.getLatitude();
			lastLocationLon = location.getLongitude();
			lastLocationTime = location.getTime();

			// �ж������Ƿ�����,�������û�������򲻻�ȡ��������
			boolean isNetValid = isNetworkConnected(FsmService.this);
			if (isNetValid) {
				// ִ���������,�ж��Ƿ���Ҫ����������������
				sendOfflineMsg(lastLocationLat, lastLocationLon,
						lastLocationTime, true);
			}

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			loggerInfo("onStatusChanged -- " + provider);
		}

		@Override
		public void onProviderEnabled(String provider) {
			loggerInfo("onProviderEnabled -- " + provider);
		}

		@Override
		public void onProviderDisabled(String provider) {

		}
	}

	/**
	 * ��ȡBeaconConfig,��������д�����ӻ����л�ȡ,���û����������ȡ,
	 * �����ȡ�����л�Ѻ͸�Beacon��ص�BeaconConfig�������������л���
	 * 
	 * @param
	 * @return
	 * @date 2014��1��21��
	 * @author trs
	 * @throws SensoroException
	 */
	private BeaconConfig getBeaconConfig(BleEvent bleEvent)
			throws SensoroException {

		// config�Ļ�ȡ˳����,�ȴӻ����ȡ,Ȼ������ݿ��ȡ,���������ȡ,������ַ�ʽ����ȡ�����������config�Ļ���
		// 1. �ӻ����ȡ
		BeaconConfig beaconConfig = cache.getBeaconConfig(bleEvent.id);
		BeaconConfigEx beaconConfigEx = null;
		if (beaconConfig == null) {
			// 2.�����ݿ��ȡ
			if (offileDBUtils != null) {
				beaconConfig = offileDBUtils.getBeaconConfig(bleEvent);
			}
			if (beaconConfig == null) {
				// 3.�������ȡBeaconConfig
				String log = null;
				if (local_log || remote_log) {
					log = LogUtils
							.getCurrentLog("getBeaconConfig from net id: "
									+ bleEvent.id);
				}
				if (local_log) {
					logger.debug(log);
				}
				if (remote_log) {
					sendUdpLogMsg(log);
				}

				beaconConfigEx = configNetUtils.getBeaconConfigEx(
						bleEvent.uuid, bleEvent.major, bleEvent.minor);
				if (beaconConfigEx != null) {
					beaconConfig = beaconConfigEx.beaconConfig;
					// ����BeaconConfig
					cache.putBeaconConfig(beaconConfig._id, beaconConfig);
					// ����������ȡ��config,��������ݿ�
					boolean res = offileDBUtils.addBeaconConfig(
							beaconConfig._id, beaconConfigEx.beaconConfigJson,
							beaconConfig.info._date);
					if (!res) {
						loggerInfo("addBeaconConfig  err");
					}
				}

			}
		}
		return beaconConfig;
	}

	/**
	 * ��ȡBeaconConfig,��������д�����ӻ����л�ȡ,���û����������ȡ,
	 * �����ȡ�����л�Ѻ͸�Beacon��ص�BeaconConfig�������������л���
	 * 
	 * @param
	 * @return
	 * @date 2014��1��21��
	 * @author trs
	 * @throws SensoroException
	 */
	private ArrayList<ZoneConfig> getZoneConfigs(BeaconConfig beaconConfig)
			throws SensoroException {

		if (beaconConfig == null || beaconConfig.zids == null) {
			return null;
		}
		ArrayList<String> zids = beaconConfig.zids;
		ArrayList<String> toNetZids = new ArrayList<String>(); // �洢��Ҫ�����������zids
		ArrayList<ZoneConfig> zoneConfigs = new ArrayList<ZoneConfig>();
		ZoneConfig zoneConfig = null;

		for (String zid : zids) {
			// 1. �ӻ����ȡZoneConfig
			zoneConfig = cache.getZoneConfig(zid);
			if (zoneConfig == null) {
				// 2. �����ݿ��ȡZoneConfig
				if (offileDBUtils != null) {
					zoneConfig = offileDBUtils.getZoneConfig(zid);
				}
				if (zoneConfig == null) {
					// 3. �������ȡZoneConfig,������ӵ��������ȡ������
					// config = configNetUtils.getZoneConfig(zid);
					toNetZids.add(zid);
				} else {
					zoneConfigs.add(zoneConfig);
				}

			} else {
				zoneConfigs.add(zoneConfig);
			}
		}
		// ��Ҫ�����������zids
		ArrayList<ZoneConfigEx> toNetConfigExs = null;
		if (toNetZids.size() != 0) {
			toNetConfigExs = configNetUtils.getZoneConfigs(toNetZids);
			if (toNetConfigExs != null) {
				for (ZoneConfigEx zoneConfigEx : toNetConfigExs) {
					zoneConfig = zoneConfigEx.zoneConfig;
					zoneConfigs.add(zoneConfig);
					// �������󵽵�ZoneConfig
					cache.putZoneConfig(zoneConfig.id, zoneConfig);
					// ���ZoneConfig�����ݿ�
					offileDBUtils.addZoneConfig(zoneConfig.id,
							zoneConfigEx.zoneConfigJson, zoneConfig._date);
				}
			}
		}

		String log = null;
		if (local_log || remote_log) {
			log = LogUtils.getCurrentLog("getZoneConfigs from net id: "
					+ beaconConfig.zids.toString());
		}
		if (local_log) {
			logger.debug(log);
		}
		if (remote_log) {
			sendUdpLogMsg(log);
		}

		return zoneConfigs;
	}

	/**
	 * ����store������¼�
	 * 
	 * @param
	 * @return
	 * @date 2014��3��18��
	 * @author trs
	 */
	private void beaconConfigTrigger(Cache cache, BeaconConfig beaconConfig,
			ZoneConfig zoneConfig, Fsm fsm, long time) {

		String log = null;

		if (fsm == null) {
			return;
		}
		if (fsm.levelinner == Fsm.LEVEL_ZONE) {
			// ״̬��������������(ZONE)
			if (!fsm.inZone) {
				// ���״̬����������Zone,����Ŀǰ����״̬����
				if (zoneConfig == null) {
					// ����ж������ϲ��ᷢ��,��Ϊ״̬����������LEVEL_ZONE��(�Է���һ)
					if (local_log || remote_log) {
						log = LogUtils.getCurrentLog("logic err");
					}
					if (local_log) {
						logger.debug(log);
					}
					if (remote_log) {
						sendUdpLogMsg(log);
					}
					return;
				}

				if (local_log || remote_log) {
					log = LogUtils.getCurrentLog("trigger : " + "enterZone");
				}
				if (local_log) {
					logger.debug(log);
				}
				if (remote_log) {
					sendUdpLogMsg(log);
				}
				// ��������spot�ص�
				sendEventMsg(zoneConfig, beaconConfig, MSG_ENTER_SPOT, -1);
				actCtrlAction(beaconConfig, zoneConfig, MSG_ENTER_SPOT_ACTION,
						-1);
				// �������zone����,�򴥷�����zone�Ļص�����
				sendEventMsg(zoneConfig, beaconConfig, MSG_ENTER_ZONE, -1);
				actCtrlAction(beaconConfig, zoneConfig, MSG_ENTER_ZONE_ACTION,
						-1);

				// ��¼ʱ��
				fsm.inZoneTime = time;
				fsm.inZone = true;
				fsm.zoneConfig = zoneConfig;

				fsm.inSpotTimeHashMap.put(beaconConfig._id, time);
				fsm.beaconConfigMap.put(beaconConfig._id, beaconConfig);

			} else {
				// �����ǰ��zone��,��ô����spot,��beaconConfigMap�в�ѯ��ӦBeaconConfig,����Ҳ���,˵�����Beacon֮ǰû�н���,����onEnterSpot
				BeaconConfig fsmBeaconConfig = fsm.beaconConfigMap
						.get(beaconConfig._id);
				if (fsmBeaconConfig == null) {
					// ֮ǰû�н������beacon
					if (local_log || remote_log) {
						log = LogUtils
								.getCurrentLog("trigger : " + "enterSpot");
					}
					if (local_log) {
						logger.debug(log);
					}
					if (remote_log) {
						sendUdpLogMsg(log);
					}
					sendEventMsg(zoneConfig, beaconConfig, MSG_ENTER_SPOT, -1);
					actCtrlAction(fsmBeaconConfig, zoneConfig,
							MSG_ENTER_SPOT_ACTION, -1);
					fsm.beaconConfigMap.put(beaconConfig._id, beaconConfig);
					fsm.inSpotTimeHashMap.put(beaconConfig._id, time);
				}
			}
		} else if (fsm.levelinner == Fsm.LEVEL_BEACON) {
			// ״̬����������Beacon
			if (beaconConfig == null) {
				return;
			}
			// �жϸ�״̬����map���Ƿ��и�beacon,�߼��Ͻ���״̬����Ӧ�ò����ָ�beacon,����߼��Է�����beacon��ID��ͬ
			boolean res = fsm.beaconConfigMap.containsKey(beaconConfig._id);
			if (!res) {
				// ���Beacon��map��û�ж�Ӧ��Beacon,�򴥷�����Spot�¼�
				sendEventMsg(null, beaconConfig, MSG_ENTER_SPOT, -1);
				actCtrlAction(beaconConfig, null, MSG_ENTER_SPOT_ACTION, -1);
				// ��¼��beacon�Ľ���ʱ��,������beacon����map
				fsm.inSpotTimeHashMap.put(beaconConfig._id, time);
				fsm.beaconConfigMap.put(beaconConfig._id, beaconConfig);
			}
		}

	}

	/**
	 * ��BeaconConfig��ӵ���Ӧfsm, BeaconConfig ---> Fsm
	 * 
	 * @param
	 * @return
	 * @date 2014��3��18��
	 * @author trs
	 */
	private void doConfig2Fsm(BeaconConfig beaconConfig, ZoneConfig zoneConfig,
			Cache cache, BleEvent bleEvent, long time) {

		Fsm fsm = null;
		String key = null;
		int level = 0;

		if (zoneConfig != null && beaconConfig.zids != null) {
			// ���ݵ�zoneConfig��beaconConfig��Ӧ��zoneConfig,������������ж�,��֤�Ϸ���,��ʵ���Ի���һ���ж�
			key = zoneConfig.id;
			level = Fsm.LEVEL_ZONE;
		} else {
			key = beaconConfig._id;
			level = Fsm.LEVEL_BEACON;
		}
		// �鿴״̬���������Ƿ���ڸ�sid��״̬��
		fsm = cache.getFsm(key);
		if (fsm == null) {
			// ���û�в����ڶ�Ӧ״̬��,�򴴽�
			fsm = new Fsm(key);
			fsm.levelinner = level;
		}

		// ���ж��Ƿ񴥷��¼�
		beaconConfigTrigger(cache, beaconConfig, zoneConfig, fsm, time);
		// ˢ��״̬��,��ֹ״̬����ʱ
		cache.putFsm(key, fsm, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
	}

	/**
	 * ����brand��BeaconConfig��ʧ
	 * 
	 * @param
	 * @return
	 * @date 2014��3��20��
	 * @author trs
	 */
	private void doConfigGone2Fsm(BeaconConfig beaconConfig,
			ZoneConfig zoneConfig, Cache cache, BleEvent bleEvent, long time) {

		Fsm fsm = null;
		String key = null;

		if (zoneConfig != null && beaconConfig.zids != null) {
			// ���ݵ�zoneConfig��beaconConfig��Ӧ��zoneConfig,������������ж�,��֤�Ϸ���,��ʵ���Ի���һ���ж�
			key = zoneConfig.id;
		} else {
			key = beaconConfig._id;
		}

		// �鿴״̬���������Ƿ���ڸ�sid��״̬��
		fsm = cache.getFsm(key);
		if (fsm == null) {
			// ���û�в����ڶ�Ӧ״̬��,�򷵻�
			return;
		}

		// ���ж��Ƿ񴥷��¼�
		beaconConfigGoneTrigger(fsm, beaconConfig, zoneConfig, time);
	}

	private void loggerInfo(String info) {
		if (DEBUG) {
			Log.i(TAG, info);
		}
	}

	/**
	 * ����Actionͳ����Ϣ
	 * 
	 * @param
	 * @return
	 * @date 2014��4��17��
	 * @author zwz
	 */
	private void sendActionCountMsg(BeaconConfig beaconConfig,
			ZoneConfig zoneConfig, int msgID, long second) {

		Message msg = actionCountHandler.obtainMessage();

		msg.what = msgID;
		Bundle bundle = new Bundle();

		if (zoneConfig != null) {
			bundle.putParcelable(ZONE_CONFIG, zoneConfig);
		}
		if (beaconConfig != null) {
			bundle.putParcelable(BEACON_CONFIG, beaconConfig);
		}
		bundle.putLong(SECONDS, second);
		msg.setData(bundle);

		actionCountHandler.sendMessage(msg);
	}

	/**
	 * ����Action��Ϣ
	 * 
	 * @param
	 * @return
	 * @date 2014��4��8��
	 * @author trs
	 */
	private void sendActionMsg(BeaconConfig beaconConfig,
			ZoneConfig zoneConfig, int msgID, long second) {

		Message msg = actionHandler.obtainMessage();

		msg.what = msgID;
		Bundle bundle = new Bundle();

		if (zoneConfig != null) {
			Zone zone = new Zone();
			zone.zid = zoneConfig.id;
			zone.param = zoneConfig.param;
			bundle.putParcelable(ZONE_CONFIG, zoneConfig);
		}
		if (beaconConfig != null) {
			bundle.putParcelable(BEACON_CONFIG, beaconConfig);
		}
		bundle.putLong(SECONDS, second);
		msg.setData(bundle);

		actionHandler.sendMessage(msg);
	}

	/**
	 * ����act�����ж��Ƿ񴥷���Ӧ��action
	 * 
	 * @param
	 * @return
	 * @date 2014��4��21��
	 * @author trs
	 */
	private void actCtrlAction(BeaconConfig beaconConfig,
			ZoneConfig zoneConfig, int actionMsgID, long second) {

		Act act = null;
		switch (actionMsgID) {
		case MSG_ENTER_SPOT_ACTION:
			if (beaconConfig == null) {
				break;
			}
			act = beaconConfig.act;
			if (act != null && act.enter != null) {
				// ���ͽ���spot��action
				sendActionMsg(beaconConfig, zoneConfig, actionMsgID, second);
			}
			break;
		case MSG_LEAVE_SPOT_ACTION:
			if (beaconConfig == null) {
				break;
			}
			act = beaconConfig.act;
			if (act != null && act.leave != null) {
				// ���ͽ���spot��action
				sendActionMsg(beaconConfig, zoneConfig, actionMsgID, second);
			}
			break;
		case MSG_STAY_SPOT_ACTION:
			if (beaconConfig == null) {
				break;
			}
			act = beaconConfig.act;
			if (act != null && act.stay != null) {
				// ���ͽ���spot��action
				sendActionMsg(beaconConfig, zoneConfig, actionMsgID, second);
			}
			break;
		case MSG_ENTER_ZONE_ACTION:
			if (zoneConfig == null) {
				break;
			}
			act = zoneConfig.act;
			if (act != null && act.stay != null) {
				// ���ͽ���spot��action
				sendActionMsg(beaconConfig, zoneConfig, actionMsgID, second);
			}
			break;
		case MSG_LEAVE_ZONE_ACTION:
			if (zoneConfig == null) {
				break;
			}
			act = zoneConfig.act;
			if (act != null && act.leave != null) {
				// ���ͽ���spot��action
				sendActionMsg(beaconConfig, zoneConfig, actionMsgID, second);
			}
			break;
		case MSG_STAY_ZONE_ACTION:
			if (zoneConfig == null) {
				break;
			}
			act = zoneConfig.act;
			if (act != null && act.stay != null) {
				// ���ͽ���spot��action
				sendActionMsg(beaconConfig, zoneConfig, actionMsgID, second);
			}
			break;

		default:
			break;
		}
	}

	/**
	 * ����Event��Ϣ
	 * 
	 * @param
	 * @return
	 * @date 2014��4��8��
	 * @author trs
	 */
	private void sendEventMsg(ZoneConfig zoneConfig, BeaconConfig beaconConfig,
			int msgId, long second) {

		if (eventHandler == null) {
			return;
		}
		Bundle bundle = new Bundle();
		bundle.putLong(SECONDS, second);
		if (zoneConfig != null) {
			Zone zone = new Zone();
			zone.zid = zoneConfig.id;
			zone.param = zoneConfig.param;
			bundle.putParcelable(ZONE, zone);
		}

		Spot spot = ClassTrans.beaconConfig2Spot(beaconConfig);
		bundle.putParcelable(SPOT, spot);

		Message msg = eventHandler.obtainMessage();
		msg.what = msgId;
		msg.setData(bundle);

		eventHandler.sendMessage(msg);
	}

	/**
	 * �����ֺ���ʧBeacon����Ϣ�ķ���
	 * 
	 * @param
	 * @return
	 * @date 2014��4��4��
	 * @author trs
	 */
	private void sendDiscoveryMsg(BleEvent bleEvent, boolean isNew) {

		if (beaconDiscoveryHandler == null) {
			return;
		}

		int msgID = -1;
		if (isNew) {
			msgID = MSG_NEW_BEACON;
		} else {
			msgID = MSG_GONE_BEACON;
		}
		Message msg = beaconDiscoveryHandler.obtainMessage(msgID, bleEvent);
		beaconDiscoveryHandler.sendMessage(msg);
	}

	/**
	 * ���ͻ�ȡ����������Ϣ
	 * 
	 * @param
	 * @return
	 * @date 2014��4��4��
	 * @author trs
	 */
	private void sendOfflineMsg(double lat, double lon, long time,
			boolean isLocation) {
		if (locationHandler == null) {
			return;
		}
		Message msg = locationHandler.obtainMessage();
		msg.what = MSG_LOCATE;
		Bundle bundle = new Bundle();
		bundle.putDouble(LAT, lat);
		bundle.putDouble(LON, lon);
		bundle.putLong(TIMESTAMP, time);
		msg.setData(bundle);

		locationHandler.sendMessage(msg);
		// if (isNetValidFlag) {// �Ƿ�����
		// Message msg = locationHandler.obtainMessage();
		// msg.what = MSG_LOCATE;
		// Bundle bundle = new Bundle();
		// bundle.putDouble(LAT, lat);
		// bundle.putDouble(LON, lon);
		// bundle.putLong(TIMESTAMP, time);
		// msg.setData(bundle);
		//
		// locationHandler.sendMessage(msg);
		// } else if (isLocation) {// ����10���ﴥ������,���һ��״̬,������1Сʱ�������߻�ȡ
		// offlinePreferences.edit()
		// .putBoolean(LAST_LOCATION_IS_DEFEATED, true).commit();
		// }
	}

	/**
	 * �ж��Ƿ�����
	 */
	public boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * �����û���id
	 * 
	 * @param
	 * @return
	 * @date 2014��3��20��
	 * @author trs
	 */
	public void bindUID(String uid) {
		this.uid = uid;
	};

	public void bindUUID(String uuid) {
		this.uuid = uuid;
	}

	public ArrayList<Zone> getZones() {

		Collection<Fsm> fsms = cache.fsmMap.values();
		ArrayList<Zone> zones = new ArrayList<Zone>();
		Zone zone = null;

		for (Fsm fsm : fsms) {
			if (fsm.inZone && fsm.zoneConfig != null) {
				zone = ClassTrans.zoneConfigtoZone(fsm.zoneConfig);
				zones.add(zone);
			}
		}
		return zones;
	}

	public ArrayList<Spot> getSpot() {

		Collection<Fsm> fsms = cache.fsmMap.values();
		ArrayList<Spot> spots = new ArrayList<Spot>();
		Collection<BeaconConfig> beaconConfigs = null;
		Spot spot = null;
		for (Fsm fsm : fsms) {
			if (fsm.inZone && fsm.beaconConfigMap != null
					&& fsm.beaconConfigMap.size() != 0) {

				beaconConfigs = fsm.beaconConfigMap.values();
				for (BeaconConfig beaconConfig : beaconConfigs) {
					spot = ClassTrans.beaconConfig2Spot(beaconConfig);
					spots.add(spot);
				}
			}
		}
		return spots;
	};

	/**
	 * ��ʼ�������߳�,��FsmService Bind�ɹ���ʱ���ʼ�������߳�
	 * 
	 * @param
	 * @return
	 * @date 2014��3��23��
	 * @author trs
	 */
	public void initHeartBeat() {
		// ��ʼ�������߳�
		initHeartBeatThread();
	};

	class BeaconConfigAll {
		BeaconConfig beaconConfig;
		String beaconConfigString;
	}

}
