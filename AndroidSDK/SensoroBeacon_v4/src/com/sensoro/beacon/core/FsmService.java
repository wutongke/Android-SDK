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
 * 用户需要继承该类完成完成对应的功能
 * 
 * @param
 * @return
 * @date 2014年3月11日
 * @author trs
 */
public abstract class FsmService extends Service {

	private static final boolean DEBUG = true;
	Logger logger = Logger.getLogger(FsmService.class);

	private static final int BLE_EVENT_MSG_NEW = 0;
	private static final int BLE_EVENT_MSG_GONE = 1;

	private static final String TAG = FsmService.class.getSimpleName();
	private Cache cache; // 容器

	private String appID;
	private String uid;
	private String uuid;
	private boolean isSticky;
	private boolean local_log;
	private boolean remote_log;
	// 网络通信类
	private ConfigNetUtils configNetUtils;
	// BleEvent停止标志
	private boolean stopFlag = false;
	// 心跳线程
	private Thread heartBeatThread;
	private HeartBeatRunnable heartBeatRunnable;
	private boolean heartBeatStopFlag;
	private BeaconHeartBeatUtils heartBeatUtils;

	// 状态机状态检查线程
	private Thread fsmStatusThread;
	private FsmStatusRunnable fsmStatusRunnable;

	// 判断是否联网线程
	private Thread networkAvailableThread;
	private NetworkAvailableRunnable networkAvailableRunnable;
	// 是否联网标志位
	// private boolean isNetValidFlag = false;
	// 存储上次定位的location和定位时间
	double lastLocationLat = 0;
	double lastLocationLon = 0;
	long lastLocationTime;
	// action网络访问以及发送Action广播的线程
	private Thread actionThread;
	private ActionRunnable actionRunnable;
	private Handler actionHandler;
	// action网络统计线程,用于离线模式
	private Thread actionCountThread;
	private ActionCountRunnable actionCountRunnable;
	private Handler actionCountHandler;
	// 消息编号 action网络访问和action网络统计线程公用
	private static final int MSG_ENTER_SPOT_ACTION = 0;
	private static final int MSG_LEAVE_SPOT_ACTION = 1;
	private static final int MSG_STAY_SPOT_ACTION = 2;
	private static final int MSG_ENTER_ZONE_ACTION = 3;
	private static final int MSG_LEAVE_ZONE_ACTION = 4;
	private static final int MSG_STAY_ZONE_ACTION = 5;
	// BleEvent广播接收器接收到BleEvent后处理BleEvent的线程
	private Handler bleEventHandler;
	private Thread bleEventThread;
	private BleEventRunnable bleEventRunnable;
	// Event处理的线程
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
	// action网络访问以及发送Action广播的线程
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

	// 是否是离线数据的标志
	boolean isOffline = false;
	// SDK SharedPreferences,这里用于存储上次获取离线数据的时间
	private SharedPreferences sharedPreferences;
	// BeaconSet,从网络获取的默认触发时间和距离信息
	BeaconSet beaconSet;

	// UPD调试
	AndroidUDP udp;
	Handler udpLoghandler;
	UdpLogRunnable udpLogRunnable;
	Thread udpLogthrThread;
	private static final int MSG_UDP = 0;

	// 位置定位回调
	OfflineLocationListener locationListener;
	LocationManager locationManager;
	Thread locationThread;
	Handler locationHandler;
	LocationRunnable locationRunnable;
	private static final int MSG_LOCATE = 0;
	private static final String LAT = "LAT";
	private static final String LON = "LON";
	private static final String TIMESTAMP = "TIMESTAMP";

	// 离线数据数据库
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
		// 初始化工具
		initUtils();
		// 初始化各种容器
		initContainer();
		// 调试信息初始化
		if (local_log) {
			initUDPLogThread();
		}
		initBleEventThread();
		// 初始化广播接收器
		initBroadcastReceiver();
		// action线程
		initActionThread();
		// action统计线程
		initActionCountThread();
		// 发现和消失Beacon线程
		initBeaconDiscoveryThread();
		// Event 线程
		initEventThread();
		// 初始化状态机状态检查线程
		initFsmStatusThread();
		// 初始化定位
		initLocation();
		// 初始化离线数据库
		initOfflineDB();
		// 开启离线数据定时获取线程
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
	 * 初始化位置回调相关
	 * 
	 * @param
	 * @return
	 * @date 2014年4月15日
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
		// 获取位置管理服务
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		// 查找到服务信息
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 低精度
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗

		String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
		// Location location = locationManager.getLastKnownLocation(provider);
		// // 通过GPS获取位置
		// 设置监听器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
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
			// 离线功能不需要心跳功能
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
				// 区分生产和测试环境
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
	 * 初始化访问后台端action的线程
	 * 
	 * @param
	 * @return
	 * @date 2014年2月18日
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
	 * 初始化访问后台端action统计的线程
	 * 
	 * @param
	 * @return
	 * @date 2014年4月17日
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
	 * 初始化访问后台端action的线程
	 * 
	 * @param
	 * @return
	 * @date 2014年2月18日
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
	 * 初始化Event的线程
	 * 
	 * @param
	 * @return
	 * @date 2014年4月8日
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
	 * 初始化工具
	 * 
	 * @param
	 * @return
	 * @date 2014年3月20日
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
	 * 开启离线数据定时获取线程
	 */
	private void initOfflineDataThread() {
		sharedPreferences = getSharedPreferences(
				SensoroSense.SENSORO_SENSE_PREF, Context.MODE_PRIVATE);
		// 模拟数据使用
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
	 * 如果存在模拟数据,则从模拟数据中加载Beaconfig的信息
	 * 
	 * @param
	 * @return
	 * @date 2014年3月10日
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
	 * 发现一个新的Beacon
	 * 
	 * @param
	 * @return
	 * @date 2014年3月11日
	 * @author trs
	 */
	abstract public void onNew(Beacon beacon);

	/**
	 * 某个Beacon消失
	 * 
	 * @param
	 * @return
	 * @date 2014年3月11日
	 * @author trs
	 */
	abstract public void onGone(Beacon beacon);

	/**
	 * 进入点
	 * 
	 * @param
	 * @return
	 * @date 2014年3月11日
	 * @author trs
	 */
	abstract public void onEnterSpot(Spot spot, Zone zone);

	/**
	 * 离开点
	 * 
	 * @param
	 * @return
	 * @date 2014年3月11日
	 * @author trs
	 */
	abstract public void onLeaveSpot(Spot spot, Zone zone);

	/**
	 * 在点停留，若一直停留，则多次回调，间隔为最小停留时间单位
	 * 
	 * @param
	 * @return
	 * @date 2014年3月11日
	 * @author trs
	 */
	abstract public void onStaySpot(Spot spot, Zone zone, long seconds);

	/**
	 * 进入区
	 * 
	 * @param
	 * @return
	 * @date 2014年3月11日
	 * @author trs
	 */
	abstract public void onEnterZone(Zone zone, Spot spot);

	/**
	 * 离开区
	 * 
	 * @param
	 * @return
	 * @date 2014年3月11日
	 * @author trs
	 */
	abstract public void onLeaveZone(Zone zone, Spot spot);

	/**
	 * 在区停留，若一直停留，则多次回调，间隔为最小停留时间单位
	 * 
	 * @param
	 * @return
	 * @date 2014年3月11日
	 * @author trs
	 */
	abstract public void onStayZone(Zone zone, Spot spot, long seconds);

	/**
	 * action回调函数
	 * 
	 * @param
	 * @return
	 * @date 2014年3月11日
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
	 * 处理BleEvent的线程
	 * 
	 * @param
	 * @return
	 * @date 2014年3月11日
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
		 * 发现新的Beacon
		 * 
		 * @param
		 * @return
		 * @date 2014年3月11日
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
				// 获取BeaconConfig之前判断该BleEvent是否在获取不到BeaconConfig的容器中
				BleEvent tmpbleEvent = cache
						.getNoBeaconConfigBleEvent(bleEvent.id);
				if (tmpbleEvent != null) {
					return;
				}
				beaconConfig = getBeaconConfig(bleEvent);
				// 如果beaconConfigs是空的,则将其加入无beaconConfigs缓冲,30分钟不在使用该bleEvent获取beaconConfigs
				if (beaconConfig == null) {
					// 加入无beaconConfigs缓冲
					cacheNoBeaconConfigBleEvent(bleEvent);
					return;
				} else {
					// 如果该beacon属于某个zone,则获取ZoneConfig
					if (beaconConfig.zids != null
							&& beaconConfig.zids.size() != 0) {
						// 获取ZoneConfig
						zoneConfigs = getZoneConfigs(beaconConfig);
						if (zoneConfigs == null) {
							// 将该zid加入获取不到ZoneConfig的容器中
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
				// 该beacon属于某个区域
				// 根据BeaconConfig处理Fsm,zid有多个，新建多个状态机
				for (ZoneConfig unit : zoneConfigs) {
					doConfig2Fsm(beaconConfig, unit, cache, bleEvent,
							bleEvent.time);
				}
			} else {
				// 该beacon不属于任何一个区域
				doConfig2Fsm(beaconConfig, null, cache, bleEvent, bleEvent.time);
			}
		}

		/**
		 * 做出beacon范围(beacon消失后)
		 * 
		 * @param
		 * @return
		 * @date 2014年3月11日
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
				// 获取BeaconConfig之前判断该BleEvent是否在获取不到BeaconConfig的容器中
				BleEvent tmpbleEvent = cache
						.getNoBeaconConfigBleEvent(bleEvent.id);
				if (tmpbleEvent != null) {
					return;
				}
				beaconConfig = getBeaconConfig(bleEvent);
				// 如果beaconConfigs是空的,则将其加入无beaconConfigs缓冲,30分钟不在使用该bleEvent获取beaconConfigs
				if (beaconConfig == null) {
					// 加入无beaconConfigs缓冲
					cacheNoBeaconConfigBleEvent(bleEvent);
					return;
				} else {
					// 如果该beacon属于某个zone,则获取ZoneConfig
					if (beaconConfig.zids != null) {
						// 获取ZoneConfig
						zoneConfigs = getZoneConfigs(beaconConfig);
						if (zoneConfigs != null) {
							for (ZoneConfig unit : zoneConfigs) {
								cache.putZoneConfig(unit.id, unit);
							}
						} else {
							// 将该zid加入获取不到ZoneConfig的容器中
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
				// 该状态机的类型是ZONE
				for (ZoneConfig unit : zoneConfigs) {
					doConfigGone2Fsm(beaconConfig, unit, cache, bleEvent, time);
				}
			} else {
				// 该状态机的类型是beacon
				doConfigGone2Fsm(beaconConfig, null, cache, bleEvent, time);
			}
		}
	}

	/**
	 * 缓存获取不到BeaconConfig的BleEvent
	 * 
	 * @param
	 * @return
	 * @date 2014年4月16日
	 * @author trs
	 */
	private void cacheNoBeaconConfigBleEvent(BleEvent bleEvent) {

		// 加入无beaconConfigs缓冲
		cache.putNoBeaconConfigBleEvent(bleEvent.id, bleEvent);
		if (DEBUG) {
			Log.v(TAG, "goneBleEvent,缓存无config beacon--major" + bleEvent.major
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

		// 发送离开店的消息,这里没有判断该beaconConfig是否在这个zone里面,逻辑上应该不用判断
		sendEventMsg(zoneConfig, beaconConfig, MSG_LEAVE_SPOT, -1);
		// 从状态机的Beacon Map中删除对应的BeaconConfig
		cacheBeaconConfig = fsm.beaconConfigMap.remove(beaconConfig._id);
		// 删除对应Beacon的停留次数
		fsm.spotStayTimesHashMap.remove(beaconConfig._id);
		// 删除对应Beacon的停留时间
		fsm.inSpotTimeHashMap.remove(beaconConfig._id);
		// 如果状态机的类型是Beacon
		if (fsm.levelinner == Fsm.LEVEL_BEACON) {
			if (fsm.beaconConfigMap.size() <= 0) {
				// 如果该Beacon Fsm的beacon数量小于等于0,逻辑上Beacon Fsm只有一个Beacon,这个判断可以不加
				cache.removeFsm(beaconConfig._id);
			}
		}
		// 发送action消息
		actCtrlAction(cacheBeaconConfig, zoneConfig, MSG_LEAVE_SPOT_ACTION, -1);
		if (fsm.beaconConfigMap.size() <= 0 && fsm.levelinner == Fsm.LEVEL_ZONE) {
			// 该状态机中的map中没有任何一个beacon,并且该状态机的类型是zone,则表示出了zone
			fsm.spotStayTimesHashMap.clear();
			fsm.inSpotTimeHashMap.clear();
			fsm.zoneStayTimes = 0;
			fsm.inZone = false;
			cache.removeFsm(zoneConfig.id);
			sendEventMsg(zoneConfig, cacheBeaconConfig, MSG_LEAVE_ZONE, -1);
			// 发送action消息
			actCtrlAction(cacheBeaconConfig, zoneConfig, MSG_LEAVE_ZONE_ACTION,
					-1);
		}
	}

	/**
	 * 状态机状态检察类,该类负责对于停留时间有限制的事件
	 * 
	 * @param
	 * @return
	 * @date 2014年3月18日
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
		 * 处理stay事件
		 * 
		 * @param
		 * @return
		 * @date 2014年3月19日
		 * @author trs
		 */
		private void doTimeOutEvent(Fsm fsm, long time) {

			long timeOut = 0;
			long space = 0;

			if (fsm.zoneConfig != null) {
				// 这个区域的配置存在stay的action
				// 该状态机是区域的状态机
				// 判断zone的停留时间
				space = time - fsm.inZoneTime;
				timeOut = beaconSet.zoneTimer * (fsm.zoneStayTimes + 1);
				// 获取当前所在的beaconconfig
				if (fsm.beaconConfigMap == null
						|| fsm.beaconConfigMap.size() == 0) {
					return;
				}
				// 获取最后一个BeaconConfig,只在最后一个beacon上面触发停留事件
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

			// 处理spot的stay事件
			if (fsm.beaconConfigMap.size() == 0) {
				// 如果beaconConfigMap中没有BeaconConfig,则不处理
			} else {
				Collection<BeaconConfig> beaconConfigs = fsm.beaconConfigMap
						.values();
				for (BeaconConfig beaconConfig : beaconConfigs) {
					if (beaconConfig != null) {
						// 判断spot的停留时间
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
							// 为0则表示,该没有进入过该beacon,是一种错误的状态,理论上不会出现这种状态
							continue;
						}
						space = time - inSpotTime;
						// 获取当前所在的beaconconfig
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
	 * 负责接收BleEvent广播,beacon的出现和消失都在这里执行
	 * 
	 * @param
	 * @return
	 * @date 2014年3月11日
	 * @author trs
	 */
	class BeaconStatusBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isNoBeaconConfigBleEvent = false;
			String action = intent.getAction();
			BleEvent bleEvent = null;
			bleEvent = intent.getParcelableExtra(BleService.BLE_EVENT);
			// 发现新的beacon
			if (bleEventHandler == null || bleEvent == null) {
				return;
			}
			// 判断该bleEvent是否在获取不到BeaconConfig的容器中,如果在其有效期内不将其放入bleEvents容器
			BleEvent noConfigBleEvent = cache
					.getNoBeaconConfigBleEvent(bleEvent.id);
			if (noConfigBleEvent != null) {
				if (DEBUG) {
					Log.v(TAG, BleService.BEACON_BROADCAST_ACTION_NEW
							+ "没有config beacon---major" + bleEvent.major
							+ "minor" + bleEvent.minor);
				}
				isNoBeaconConfigBleEvent = true;
			}
			if (action.equals(BleService.BEACON_BROADCAST_ACTION_NEW)) {
				// HW层发现新的beacon,触发onNew的回调
				sendDiscoveryMsg(bleEvent, true);

				if (!isNoBeaconConfigBleEvent) {
					// 发送发现新的beacon的消息
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
				// HW层发现新的beacon,触发onGone的回调
				sendDiscoveryMsg(bleEvent, false);

				if (!isNoBeaconConfigBleEvent) {
					// beacon消失
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

	// 发送action的Runnable
	class ActionRunnable implements Runnable {

		@Override
		public void run() {
			Looper.prepare();
			actionHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					int what = msg.what;
					boolean remote = true; // 默认从网络获取action逻辑

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
		 * 本地触发action回调,但是无论从本地还是层网络触发sdk回调,都要进行网络访问,这样做是为了数据统计的需要
		 * 
		 * @param
		 * @return
		 * @date 2014年4月16日
		 * @author trs
		 */
		private boolean localTrigger(ZoneConfig zoneConfig,
				BeaconConfig beaconConfig, int msgID, long seconds) {

			// 获取对应action之前触发的信息
			if (dataBaseAdpter == null) {
				return false;
			}
			// 获取type和name,type区别zone或spot,name区别enter,leave,stay
			// 获取id,如果是zone则使用zid,如果是spot使用beacon.id,不管是那种id都是app自己定义的
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
					// 没有查询到actionRecord.则说明之前没有触发过该action,判断act的逻辑,并且将该action加入数据库
					// 判断每天触发的频次
					if (actEvent.freq.daily > 0) {
						// 每天触发的频次如果大于0,触发action
						action = getAction(type, name, actEvent, beaconConfig,
								zoneConfig);
						onAction(action);
						// 添加数据库
						res = offileDBUtils.addActionRecord(id, type, name,
								new Date().getTime(), 1);
						if (!res) {
							// 更新数据库失败
							loggerInfo("addActionRecord err");
						}
					}
				} else {
					// 查询到对应的actionRecord,则进行频次业务逻辑判断,每天的频次控制和时间间隔的控制不同时存在

					// 如果两个频次控制都是-1.则无限制触发action
					Date now = new Date();
					DateBrief lastDate = DateUtils
							.getDate(actionRecord.timestamp);
					DateBrief date = DateUtils.getDate(now);

					if (actEvent.freq.daily == -1
							&& actEvent.freq.interval == -1) {
						action = getAction(type, name, actEvent, beaconConfig,
								zoneConfig);
						onAction(action);
						// //数据库更新action触发次数
						int result = offileDBUtils.updateActionRecord(id, type,
								name, new Date().getTime(),
								actionRecord.times++);
						loggerInfo("updateActionRecord result -- " + result);
						sendActionCountMsg(beaconConfig, zoneConfig, msgID, -1);
					} else if (actEvent.freq.daily != -1) {
						// 进行每天的频次判断,daily为-1表示没有每天的频次控制
						// 如果日期不是同一天,则重新计算频次
						if (lastDate.year == date.year
								&& lastDate.day == date.day
								&& lastDate.month == date.month) {
							// 如果是同一天,比较频次
							if (actionRecord.times >= actEvent.freq.daily) {
								// 数据库记录action的触发次数大于或等于每天的触发频次,则不触发action
							} else {
								// 数据库更新action触发次数
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
							// 如果日期不是同一天,则重新计算频次
							// 将触发频次置成1
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
						// 进行时间间隔的频次判断,如果interval为-1表示没有时间间隔频次控制
						now = new Date();
						long space = now.getTime() - actionRecord.timestamp;
						if (space > actEvent.freq.interval) {
							// 如果时间间隔大于interval则触发
							action = getAction(type, name, actEvent,
									beaconConfig, zoneConfig);
							onAction(action);
							// 更新数据库
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
			// 区分生产sdk环境和测试环境
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
					// 将actionBrief 转换成action
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

	// beacon出现和消失的Runnable
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
						// 发现一个Beacon的回调
						beacon = BleEvent.toBeacon(bleEvent);
						onNew(beacon);
						break;

					case MSG_GONE_BEACON:
						// 消失一个Beacon的回调
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

	// Event触发的Runnable
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
									// 如果存储成功,记录存储成功时间
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
	 * 网络未连接状态下,每隔数秒判断网络是否连接成功 不成功继续监测,成功则获取离线数据
	 * 
	 * @param
	 * @return
	 * @date 2014年3月18日
	 * @author trs
	 */
	class NetworkAvailableRunnable implements Runnable {

		@Override
		public void run() {
			while (true) {

				// 判断网络是否连接,如果连接了则进行网络通信,获取离线数据
				boolean isNetValid = isNetworkConnected(FsmService.this);
				if (isNetValid) {
					Long offlineTime = sharedPreferences.getLong(
							SensoroSense.LAST_OFFLINE_DATA_TIME, -1);

					// 获取是否是由地理位置变化引起的获取离线数据请求
					boolean isLocationToGet = sharedPreferences.getBoolean(
							SensoroSense.IS_LOCATION_TO_GET, false);
					// 如果是由于地理位置和时间的变化引起的
					if (isLocationToGet
							|| (offlineTime > 0 && offlineTime < System
									.currentTimeMillis()
									+ SensoroSense.GET_OFFLINE_PERIOD)) {
						// 执行网络情况,判断是否需要进行网络数据下载
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

			// 存储定位的信息,如果当前网络不通或者获取离线数据失败,提供给下次网络获取显示数据使用
			lastLocationLat = location.getLatitude();
			lastLocationLon = location.getLongitude();
			lastLocationTime = location.getTime();

			// 判断网络是否连接,如果网络没有连接则不获取离线数据
			boolean isNetValid = isNetworkConnected(FsmService.this);
			if (isNetValid) {
				// 执行网络情况,判断是否需要进行网络数据下载
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
	 * 获取BeaconConfig,如果缓存中存在则从缓存中获取,如果没有则从网络获取,
	 * 网络获取过程中会把和该Beacon相关的BeaconConfig都下载下来进行缓存
	 * 
	 * @param
	 * @return
	 * @date 2014年1月21日
	 * @author trs
	 * @throws SensoroException
	 */
	private BeaconConfig getBeaconConfig(BleEvent bleEvent)
			throws SensoroException {

		// config的获取顺序是,先从缓存读取,然后从数据库读取,最后从网络获取,如果三种方式都获取不到则加入无config的缓存
		// 1. 从缓存读取
		BeaconConfig beaconConfig = cache.getBeaconConfig(bleEvent.id);
		BeaconConfigEx beaconConfigEx = null;
		if (beaconConfig == null) {
			// 2.从数据库读取
			if (offileDBUtils != null) {
				beaconConfig = offileDBUtils.getBeaconConfig(bleEvent);
			}
			if (beaconConfig == null) {
				// 3.从网络获取BeaconConfig
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
					// 缓存BeaconConfig
					cache.putBeaconConfig(beaconConfig._id, beaconConfig);
					// 如果从网络获取到config,则存入数据库
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
	 * 获取BeaconConfig,如果缓存中存在则从缓存中获取,如果没有则从网络获取,
	 * 网络获取过程中会把和该Beacon相关的BeaconConfig都下载下来进行缓存
	 * 
	 * @param
	 * @return
	 * @date 2014年1月21日
	 * @author trs
	 * @throws SensoroException
	 */
	private ArrayList<ZoneConfig> getZoneConfigs(BeaconConfig beaconConfig)
			throws SensoroException {

		if (beaconConfig == null || beaconConfig.zids == null) {
			return null;
		}
		ArrayList<String> zids = beaconConfig.zids;
		ArrayList<String> toNetZids = new ArrayList<String>(); // 存储需要向网络情况的zids
		ArrayList<ZoneConfig> zoneConfigs = new ArrayList<ZoneConfig>();
		ZoneConfig zoneConfig = null;

		for (String zid : zids) {
			// 1. 从缓存读取ZoneConfig
			zoneConfig = cache.getZoneConfig(zid);
			if (zoneConfig == null) {
				// 2. 从数据库读取ZoneConfig
				if (offileDBUtils != null) {
					zoneConfig = offileDBUtils.getZoneConfig(zid);
				}
				if (zoneConfig == null) {
					// 3. 从网络获取ZoneConfig,将其添加到从网络获取的数组
					// config = configNetUtils.getZoneConfig(zid);
					toNetZids.add(zid);
				} else {
					zoneConfigs.add(zoneConfig);
				}

			} else {
				zoneConfigs.add(zoneConfig);
			}
		}
		// 需要向网络请求的zids
		ArrayList<ZoneConfigEx> toNetConfigExs = null;
		if (toNetZids.size() != 0) {
			toNetConfigExs = configNetUtils.getZoneConfigs(toNetZids);
			if (toNetConfigExs != null) {
				for (ZoneConfigEx zoneConfigEx : toNetConfigExs) {
					zoneConfig = zoneConfigEx.zoneConfig;
					zoneConfigs.add(zoneConfig);
					// 缓存请求到的ZoneConfig
					cache.putZoneConfig(zoneConfig.id, zoneConfig);
					// 添加ZoneConfig到数据库
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
	 * 触发store层面的事件
	 * 
	 * @param
	 * @return
	 * @date 2014年3月18日
	 * @author trs
	 */
	private void beaconConfigTrigger(Cache cache, BeaconConfig beaconConfig,
			ZoneConfig zoneConfig, Fsm fsm, long time) {

		String log = null;

		if (fsm == null) {
			return;
		}
		if (fsm.levelinner == Fsm.LEVEL_ZONE) {
			// 状态机的类型是区域(ZONE)
			if (!fsm.inZone) {
				// 如果状态机的类型是Zone,并且目前不在状态机中
				if (zoneConfig == null) {
					// 这个判断理论上不会发生,因为状态机的类型是LEVEL_ZONE了(以防万一)
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
				// 触发进入spot回调
				sendEventMsg(zoneConfig, beaconConfig, MSG_ENTER_SPOT, -1);
				actCtrlAction(beaconConfig, zoneConfig, MSG_ENTER_SPOT_ACTION,
						-1);
				// 如果不在zone里面,则触发进入zone的回调函数
				sendEventMsg(zoneConfig, beaconConfig, MSG_ENTER_ZONE, -1);
				actCtrlAction(beaconConfig, zoneConfig, MSG_ENTER_ZONE_ACTION,
						-1);

				// 记录时间
				fsm.inZoneTime = time;
				fsm.inZone = true;
				fsm.zoneConfig = zoneConfig;

				fsm.inSpotTimeHashMap.put(beaconConfig._id, time);
				fsm.beaconConfigMap.put(beaconConfig._id, beaconConfig);

			} else {
				// 如果当前在zone中,那么处理spot,从beaconConfigMap中查询对应BeaconConfig,如果找不到,说明这个Beacon之前没有进入,触发onEnterSpot
				BeaconConfig fsmBeaconConfig = fsm.beaconConfigMap
						.get(beaconConfig._id);
				if (fsmBeaconConfig == null) {
					// 之前没有进入过该beacon
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
			// 状态机的类型是Beacon
			if (beaconConfig == null) {
				return;
			}
			// 判断该状态机的map中是否有该beacon,逻辑上讲该状态机中应该不会又该beacon,这个逻辑以防两个beacon的ID相同
			boolean res = fsm.beaconConfigMap.containsKey(beaconConfig._id);
			if (!res) {
				// 如果Beacon的map中没有对应的Beacon,则触发进入Spot事件
				sendEventMsg(null, beaconConfig, MSG_ENTER_SPOT, -1);
				actCtrlAction(beaconConfig, null, MSG_ENTER_SPOT_ACTION, -1);
				// 记录该beacon的进入时间,并将该beacon加入map
				fsm.inSpotTimeHashMap.put(beaconConfig._id, time);
				fsm.beaconConfigMap.put(beaconConfig._id, beaconConfig);
			}
		}

	}

	/**
	 * 将BeaconConfig添加到对应fsm, BeaconConfig ---> Fsm
	 * 
	 * @param
	 * @return
	 * @date 2014年3月18日
	 * @author trs
	 */
	private void doConfig2Fsm(BeaconConfig beaconConfig, ZoneConfig zoneConfig,
			Cache cache, BleEvent bleEvent, long time) {

		Fsm fsm = null;
		String key = null;
		int level = 0;

		if (zoneConfig != null && beaconConfig.zids != null) {
			// 传递的zoneConfig是beaconConfig对应的zoneConfig,这里进行两个判断,保证合法性,其实可以换成一个判断
			key = zoneConfig.id;
			level = Fsm.LEVEL_ZONE;
		} else {
			key = beaconConfig._id;
			level = Fsm.LEVEL_BEACON;
		}
		// 查看状态机集合中是否存在该sid的状态机
		fsm = cache.getFsm(key);
		if (fsm == null) {
			// 如果没有不存在对应状态机,则创建
			fsm = new Fsm(key);
			fsm.levelinner = level;
		}

		// 先判断是否触发事件
		beaconConfigTrigger(cache, beaconConfig, zoneConfig, fsm, time);
		// 刷新状态机,防止状态机超时
		cache.putFsm(key, fsm, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
	}

	/**
	 * 处理brand层BeaconConfig消失
	 * 
	 * @param
	 * @return
	 * @date 2014年3月20日
	 * @author trs
	 */
	private void doConfigGone2Fsm(BeaconConfig beaconConfig,
			ZoneConfig zoneConfig, Cache cache, BleEvent bleEvent, long time) {

		Fsm fsm = null;
		String key = null;

		if (zoneConfig != null && beaconConfig.zids != null) {
			// 传递的zoneConfig是beaconConfig对应的zoneConfig,这里进行两个判断,保证合法性,其实可以换成一个判断
			key = zoneConfig.id;
		} else {
			key = beaconConfig._id;
		}

		// 查看状态机集合中是否存在该sid的状态机
		fsm = cache.getFsm(key);
		if (fsm == null) {
			// 如果没有不存在对应状态机,则返回
			return;
		}

		// 先判断是否触发事件
		beaconConfigGoneTrigger(fsm, beaconConfig, zoneConfig, time);
	}

	private void loggerInfo(String info) {
		if (DEBUG) {
			Log.i(TAG, info);
		}
	}

	/**
	 * 发送Action统计消息
	 * 
	 * @param
	 * @return
	 * @date 2014年4月17日
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
	 * 发送Action消息
	 * 
	 * @param
	 * @return
	 * @date 2014年4月8日
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
	 * 根据act属性判断是否触发对应的action
	 * 
	 * @param
	 * @return
	 * @date 2014年4月21日
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
				// 发送进入spot的action
				sendActionMsg(beaconConfig, zoneConfig, actionMsgID, second);
			}
			break;
		case MSG_LEAVE_SPOT_ACTION:
			if (beaconConfig == null) {
				break;
			}
			act = beaconConfig.act;
			if (act != null && act.leave != null) {
				// 发送进入spot的action
				sendActionMsg(beaconConfig, zoneConfig, actionMsgID, second);
			}
			break;
		case MSG_STAY_SPOT_ACTION:
			if (beaconConfig == null) {
				break;
			}
			act = beaconConfig.act;
			if (act != null && act.stay != null) {
				// 发送进入spot的action
				sendActionMsg(beaconConfig, zoneConfig, actionMsgID, second);
			}
			break;
		case MSG_ENTER_ZONE_ACTION:
			if (zoneConfig == null) {
				break;
			}
			act = zoneConfig.act;
			if (act != null && act.stay != null) {
				// 发送进入spot的action
				sendActionMsg(beaconConfig, zoneConfig, actionMsgID, second);
			}
			break;
		case MSG_LEAVE_ZONE_ACTION:
			if (zoneConfig == null) {
				break;
			}
			act = zoneConfig.act;
			if (act != null && act.leave != null) {
				// 发送进入spot的action
				sendActionMsg(beaconConfig, zoneConfig, actionMsgID, second);
			}
			break;
		case MSG_STAY_ZONE_ACTION:
			if (zoneConfig == null) {
				break;
			}
			act = zoneConfig.act;
			if (act != null && act.stay != null) {
				// 发送进入spot的action
				sendActionMsg(beaconConfig, zoneConfig, actionMsgID, second);
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 发送Event消息
	 * 
	 * @param
	 * @return
	 * @date 2014年4月8日
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
	 * 负责发现和消失Beacon的消息的发送
	 * 
	 * @param
	 * @return
	 * @date 2014年4月4日
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
	 * 发送获取离线数据消息
	 * 
	 * @param
	 * @return
	 * @date 2014年4月4日
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
		// if (isNetValidFlag) {// 是否联网
		// Message msg = locationHandler.obtainMessage();
		// msg.what = MSG_LOCATE;
		// Bundle bundle = new Bundle();
		// bundle.putDouble(LAT, lat);
		// bundle.putDouble(LON, lon);
		// bundle.putLong(TIMESTAMP, time);
		// msg.setData(bundle);
		//
		// locationHandler.sendMessage(msg);
		// } else if (isLocation) {// 若是10公里触发离线,测存一个状态,优先于1小时触发离线获取
		// offlinePreferences.edit()
		// .putBoolean(LAST_LOCATION_IS_DEFEATED, true).commit();
		// }
	}

	/**
	 * 判断是否联网
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
	 * 设置用户的id
	 * 
	 * @param
	 * @return
	 * @date 2014年3月20日
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
	 * 初始化心跳线程,当FsmService Bind成功的时候初始化心跳线程
	 * 
	 * @param
	 * @return
	 * @date 2014年3月23日
	 * @author trs
	 */
	public void initHeartBeat() {
		// 初始化心跳线程
		initHeartBeatThread();
	};

	class BeaconConfigAll {
		BeaconConfig beaconConfig;
		String beaconConfigString;
	}

}
