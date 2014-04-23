package com.sensoro.beacon.core;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.sensoro.beacon.base.IBeacon;
import com.sensoro.beacon.base.IBeaconConsumer;
import com.sensoro.beacon.base.RangeNotifier;
import com.sensoro.beacon.base.Region;
import com.sensoro.beacon.base.SensoroIBeaconManager;
import com.sensoro.beacon.base.WifiBeacon;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public final class BleService extends Service implements IBeaconConsumer {

	private static final boolean DEBUG = true;
	// �����µ�beacon�����߳�beacon��Χ�Ĺ㲥action
	public static final String BEACON_BROADCAST_ACTION_NEW = "BEACON_BROADCAST_ACTION_NEW";
	public static final String BEACON_BROADCAST_ACTION_GONE = "BEACON_BROADCAST_ACTION_GONE";
	public static final String BLE_EVENT = "BLE_EVENT";

	private static final String TAG = BleService.class.getSimpleName();
	private static final long WIFI_SCAN_SPACE = 3 * 1000;
	private static final int BLE_EVENT_MAP_MAX_CAPACITY = 10;
	private static final long CHECK_BLE_EVENT_TIMEOUT_SPACE = 1000;

	// �ػ�����,�жϽ���Beacon��Χ
	private Thread daemonThread;
	private Runnable daemonRunnable;
	private boolean daemonStopFlag = false;
	private ConcurrentLinkedHashMap<String, BleEvent> bleEventMap;
	private SensoroIBeaconManager beaconManager;
	private RangeNotifier rangeNotifier;
	private Region region = null;

	// wifi
	private WifiManager wifiManager = null;
	private WifiReceiver wifiReceiver = null;
	private List<ScanResult> wifiScanResults = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// ��ʼ��BleEvent����
		initContainer();
		// �����ػ�����
		initDaemonThread();
		// ��ʼ��beacon����
		initIBeacon();
		// START_STICKY ��֤service��ɱ�����ܹ�����
		return START_STICKY;
	}

	@Override
	public void onIBeaconServiceConnect() {
		startRange();
	}

	private void initDaemonThread() {
		if (daemonRunnable == null) {
			daemonRunnable = new DaemonRunnable();
			daemonThread = new Thread(daemonRunnable);
			daemonThread.start();
		}
	}

	/**
	 * ibeacon init
	 * 
	 * @param
	 * @return
	 * @date 2014��1��21��
	 * @author trs
	 */
	private void initIBeacon() {
		// �ж�ϵͳ�汾,���֧��BLE,ʹ��BLE Beacon;����,ʹ��wifi Beacon
		if (getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			if (beaconManager == null) {
				beaconManager = SensoroIBeaconManager
						.getInstanceForApplication(this);
				rangeNotifier = new RangeNotifier() {

					@Override
					public void didRangeBeaconsInRegion(
							Collection<IBeacon> arg0, Region arg1) {
						dealIBeacons(arg0, new Date().getTime());
					}
				};
				region = new Region("sensoroBLE", null, null, null);
				beaconManager.bind(this);
			}
		} else {
			wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
			wifiReceiver = new WifiReceiver();
			registerReceiver(wifiReceiver, new IntentFilter(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			wifiManager.startScan();
		}
	}

	private void dealIBeacons(Collection<IBeacon> iBeacons, long time) {
		for (IBeacon iBeacon : iBeacons) {
			// ��HW��IBeaconת����BleEvent
			BleEvent bleEvent = getBleEvent(iBeacon, time);
			// ���û�ж�Ӧ(key,value)�����,�����ͷ�����beacon�Ĺ㲥;����������
			String key = bleEvent.id;
			if (!bleEventMap.containsKey(key)) {
				// û�ж�Ӧ��beacon,���͹㲥,֪ͨFsmService�����µ�beacon
				sendBeaconBroadcast(BEACON_BROADCAST_ACTION_NEW, bleEvent);
			}
			bleEventMap.put(bleEvent.id, bleEvent);
		}
	}

	private BleEvent getBleEvent(IBeacon iBeacon, long time) {
		BleEvent bleEvent = new BleEvent(iBeacon, time);
		return bleEvent;
	}

	/**
	 * ��ʼɨ��ibeacon�豸
	 * 
	 * @param
	 * @return
	 * @date 2014��1��21��
	 * @author trs
	 */
	private boolean startRange() {
		if (beaconManager == null || region == null) {
			loggerInfo("beaconManager null");
			return false;
		}
		try {
			beaconManager.setRangeNotifier(rangeNotifier);
			beaconManager
					.setForegroundBetweenScanPeriod(SensoroSense.DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD);
			beaconManager
					.setForegroundScanPeriod(SensoroSense.DEFAULT_FOREGROUND_SCAN_PERIOD);
			beaconManager
					.setBackgroundScanPeriod(SensoroSense.DEFAULT_BACKGROUND_SCAN_PERIOD);
			beaconManager
					.setBackgroundBetweenScanPeriod(SensoroSense.DEFAULT_BACKGROUND_BETWEEN_SCAN_PERIOD);
			beaconManager.startRangingBeaconsInRegion(region);
		} catch (RemoteException e) {
			loggerInfo(e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	private void initContainer() {
		if (bleEventMap == null) {
			bleEventMap = new ConcurrentLinkedHashMap.Builder<String, BleEvent>()
					.maximumWeightedCapacity(BLE_EVENT_MAP_MAX_CAPACITY)
					.build();
		}
	}

	/**
	 * ���beacon�Ƿ�ʱ���߳�,�������beacon��ʱ�򽫸�beacon��map��ɾ��
	 * 
	 * @date 2014��3��11��
	 */
	class DaemonRunnable implements Runnable {

		@Override
		public void run() {
			while (true) {
				if (daemonStopFlag) {
					break;
				}
				// ÿ��1��ȡ�ж�bleEvent�����Ƿ��й���
				long time = new Date().getTime();
				Set<String> keySet = bleEventMap.keySet();
				for (String key : keySet) {
					BleEvent bleEvent = bleEventMap.get(key);
					if (bleEvent == null) {
						continue;
					}
					// long currentTime = System.nanoTime() / 1000000;
					// long space = currentTime - bleEvent.time;
					long space = time - bleEvent.time;
					if (space > SensoroSense.BEACON_GONE_TIME) {
						Log.v(TAG, "send onGone broadcast---major:"+bleEvent.major+"minor:"+bleEvent.minor);
						// ����beacon��ʧ�Ĺ㲥
						sendBeaconBroadcast(BEACON_BROADCAST_ACTION_GONE,
								bleEvent);
						bleEventMap.remove(key);
					}
				}
				try {
					Thread.sleep(CHECK_BLE_EVENT_TIMEOUT_SPACE);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	}

	private void loggerInfo(String info) {
		if (DEBUG) {
			Log.i(TAG, info);
		}
	}

	/**
	 * ����beacon���ֻ�����ʧ�Ĺ㲥
	 * 
	 * @param
	 * @return
	 * @date 2014��3��11��
	 * @author trs
	 */
	private void sendBeaconBroadcast(String action, BleEvent bleEvent) {
		if (action == null) {
			return;
		}
		Intent intent = new Intent(action);
		intent.putExtra(BLE_EVENT, bleEvent);
		sendBroadcast(intent);
	}

	/**
	 * wifi beaconɨ�����㲥������
	 * 
	 * @param
	 * @return
	 * @date 2014��4��2��
	 * @author zwz
	 */
	class WifiReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			wifiScanResults = wifiManager.getScanResults();
			Log.d(TAG, "wifiInfo" + wifiScanResults.toString());
			// Ŀǰ��BroadcastReceiver����,�����ʱ,����handler�����߳��д���
			dealIBeacons(wifiScanResults, new Date().getTime());
			new Thread(wifiScanRunnable).start();
		}
	}

	/**
	 * wifi ɨ���߳�,ɨ���� 5s
	 * 
	 * @param
	 * @return
	 * @date 2014��4��2��
	 * @author zwz
	 */
	private Runnable wifiScanRunnable = new Runnable() {

		@Override
		public void run() {
			try {
				Thread.sleep(WIFI_SCAN_SPACE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			wifiManager.startScan();
		}
	};

	private void dealIBeacons(List<ScanResult> wifiScanResults, long time) {
		for (ScanResult wifiScanResult : wifiScanResults) {
			// ��WifiBeaconɨ����ת����BleEvent
			BleEvent bleEvent = getWifiBleEvent(wifiScanResult.SSID, time);
			if (bleEvent != null) {
				// ���û�ж�Ӧ(key,value)�����,�����ͷ�����beacon�Ĺ㲥;����������
				String key = bleEvent.id;
				if (!bleEventMap.containsKey(key)) {
					// û�ж�Ӧ��beacon,���͹㲥,֪ͨFsmService�����µ�beacon
					sendBeaconBroadcast(BEACON_BROADCAST_ACTION_NEW, bleEvent);
				}
				bleEventMap.put(bleEvent.id, bleEvent);
			}
		}
	}

	/**
	 * ��ɨ���WifiBeaconת����BleEvent
	 * 
	 * @param
	 * @return
	 * @date 2014��4��2��
	 * @author zwz
	 */
	private BleEvent getWifiBleEvent(String ssid, long time) {
		BleEvent bleEvent = null;
		WifiBeacon wifiBeacon = WIFIBeaconUtils.GetWifiBeaconBySSID(ssid);
		if (wifiBeacon != null) {
			bleEvent = new BleEvent(wifiBeacon, time);
		}
		return bleEvent;
	}
}
