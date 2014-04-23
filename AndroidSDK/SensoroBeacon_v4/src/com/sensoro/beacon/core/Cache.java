package com.sensoro.beacon.core;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import android.util.Log;

class Cache {

	private static final String TAG = Cache.class.getSimpleName();
	Logger logger = Logger.getLogger(Cache.class);
	private boolean DEBUG = true;

	// ״̬������
	ConcurrentHashMap<String, Fsm> fsmMap; // ״̬��
	// bleEventʱ���������
	ConcurrentHashMap<String, BeaconConfig> beaconConfigMap; // BeaconConfig�Ļ���
	ConcurrentHashMap<String, ZoneConfig> zoneConfigMap; // BeaconConfig�Ļ���
	ConcurrentHashMap<String, BleEvent> noBeaconConfigBleEventMap; // ��ȡ����beaconConfig��BleEvent�Ļ���,��Чʱ��min
	ConcurrentHashMap<String, String> noZoneConfigMap; // ��ȡ����ZoneConfig�Ļ���,��Чʱ��min
	private DelayQueue<DelayItem<Pair<String, Object>>> delayQueue; // ���ڿ��Ƴ�ʱ��DelayQueue,ͬʱ����״̬���ͻ���ĳ�ʱ

	private Thread daemonThread; // �ػ�����

	public Cache() {

		InitContainer();
		InitDaemonThread();
	}

	private void daemonCheck() {

		LoggerInfo("cache service started");
		while (true) {
			try {
				// ��ȡ��ʱ
				// in��out�ĵ�״̬�������ʱ����,������״̬������»ض�Ӧconfδ������״̬
				DelayItem<Pair<String, Object>> delayItem = delayQueue.take();
				if (delayItem != null) {
					// ��ʱ������
					// ���ݳ�ʱɾ����Ӧ��״̬��
					Pair<String, Object> pair = delayItem.getItem();
					if (pair.value instanceof Fsm) {
						fsmMap.remove(pair.key, pair.value);
					} else if (pair.value instanceof BeaconConfig) {
						beaconConfigMap.remove(pair.key, pair.value);
					} else if (pair.value instanceof ZoneConfig) {
						zoneConfigMap.remove(pair.key, pair.value);
					}
				}
			} catch (InterruptedException e) {
				LoggerInfo(e.getMessage());
				break;
			}
		}
		LoggerInfo("cache service stopped.");
	}

	private void InitDaemonThread() {
		// �ػ�����,����״̬����ʱ����
		Runnable daemonTask = new Runnable() {
			public void run() {
				daemonCheck();
			}
		};

		daemonThread = new Thread(daemonTask);
		daemonThread.setDaemon(true);
		daemonThread.setName("Cache Daemon");
		daemonThread.start();
	}

	/**
	 * ��ʱ��������
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	private void InitContainer() {
		// ״̬��
		fsmMap = new ConcurrentHashMap<String, Fsm>();
		// beaconConfig�¼�
		beaconConfigMap = new ConcurrentHashMap<String, BeaconConfig>();
		delayQueue = new DelayQueue<DelayItem<Pair<String, Object>>>();
		noBeaconConfigBleEventMap = new ConcurrentHashMap<String, BleEvent>();
		zoneConfigMap = new ConcurrentHashMap<String, ZoneConfig>();
	}

	private boolean removeByKeyValue(String key, Object value) {
		DelayItem<Pair<String, Object>> item = new DelayItem<Pair<String, Object>>(
				new Pair<String, Object>(key, value), 0);
		boolean isRemove = delayQueue.remove(item);

		return isRemove;
	}

	/**
	 * ���Store״̬��,���Կ��Ƴ�ʱʱ��
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public void putFsm(String key, Fsm value, long time, TimeUnit unit) {
		Fsm oldValue = fsmMap.put(key, value);
		if (oldValue != null) {
			removeByKeyValue(key, value);
		}

		long nanoTime = TimeUnit.NANOSECONDS.convert(time, unit);
		delayQueue.put(new DelayItem<Pair<String, Object>>(
				new Pair<String, Object>(key, value), nanoTime));
	}

	/**
	 * ���BleEvent�¼�,���Կ��Ƴ�ʱʱ��
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public void putBeaconConfig(String key, BeaconConfig value, long time,
			TimeUnit unit) {
		BeaconConfig oldValue = beaconConfigMap.put(key, value);
		if (oldValue != null) {
			removeByKeyValue(key, value);
		}

		long nanoTime = TimeUnit.NANOSECONDS.convert(time, unit);
		delayQueue.put(new DelayItem<Pair<String, Object>>(
				new Pair<String, Object>(key, value), nanoTime));
	}

	/**
	 * ���BleEvent�¼�,���Կ��Ƴ�ʱʱ��
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public void putZoneConfig(String key, ZoneConfig value, long time,
			TimeUnit unit) {

		ZoneConfig oldValue = zoneConfigMap.put(key, value);
		if (oldValue != null) {
			removeByKeyValue(key, value);
		}

		long nanoTime = TimeUnit.NANOSECONDS.convert(time, unit);
		delayQueue.put(new DelayItem<Pair<String, Object>>(
				new Pair<String, Object>(key, value), nanoTime));
	}

	/**
	 * ���Store״̬��,ʹ��Ĭ�ϳ�ʱʱ��
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public void putFsm(String key, Fsm value) {

		if (key == null) {
			logger.debug("putFsm key is null");
			return;
		}
		Fsm oldValue = fsmMap.put(key, value);
		if (oldValue != null) {
			removeByKeyValue(key, value);
		}
		// Ĭ�ϵ�store��ʱ
		long nanoTime = TimeUnit.NANOSECONDS.convert(
				SensoroSense.FSM_TIMEOUT_DEFAULT, TimeUnit.MILLISECONDS);
		delayQueue.put(new DelayItem<Pair<String, Object>>(
				new Pair<String, Object>(key, value), nanoTime));
	}

	/**
	 * ���BeaconConfig
	 * 
	 * @param
	 * @return BeaconConfig�������ӵĸ���
	 * @date 2014��2��6��
	 * @author trs
	 */
	public int putBeaconConfig(String key, BeaconConfig value) {

		boolean isRemove = true;

		BeaconConfig oldValue = beaconConfigMap.put(key, value);
		if (oldValue != null) {
			isRemove = removeByKeyValue(key, value);
		}

		long nanoTime = TimeUnit.NANOSECONDS.convert(
				SensoroSense.BEACONCONFIG_TIMEOUT, TimeUnit.MILLISECONDS);
		delayQueue.put(new DelayItem<Pair<String, Object>>(
				new Pair<String, Object>(key, value), nanoTime));
		if (isRemove) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * ���BeaconConfig
	 * 
	 * @param
	 * @return BeaconConfig�������ӵĸ���
	 * @date 2014��2��6��
	 * @author trs
	 */
	public int putZoneConfig(String key, ZoneConfig value) {

		boolean isRemove = true;

		ZoneConfig oldValue = zoneConfigMap.put(key, value);
		if (oldValue != null) {
			isRemove = removeByKeyValue(key, value);
		}

		long nanoTime = TimeUnit.NANOSECONDS.convert(
				SensoroSense.ZONECONFIG_TIMEOUT, TimeUnit.MILLISECONDS);
		delayQueue.put(new DelayItem<Pair<String, Object>>(
				new Pair<String, Object>(key, value), nanoTime));
		if (isRemove) {
			return 0;
		} else {
			return 1;
		}
	}

	public void putNoBeaconConfigBleEvent(String key, BleEvent bleEvent) {
		BleEvent oldValue = noBeaconConfigBleEventMap.put(key, bleEvent);
		if (oldValue != null) {
			removeByKeyValue(key, bleEvent);
		}
		long nanoTime = TimeUnit.NANOSECONDS.convert(
				SensoroSense.NO_BEACONCONFIG_TIMEOUT, TimeUnit.MILLISECONDS);
		delayQueue.put(new DelayItem<Pair<String, Object>>(
				new Pair<String, Object>(key, bleEvent), nanoTime));
	}

	public void putNoZoneConfigID(String key, String value) {
		String oldValue = noZoneConfigMap.put(key, value);
		if (oldValue != null) {
			removeByKeyValue(key, value);
		}
		long nanoTime = TimeUnit.NANOSECONDS.convert(
				SensoroSense.NO_ZONECONFIG_TIMEOUT, TimeUnit.MILLISECONDS);
		delayQueue.put(new DelayItem<Pair<String, Object>>(
				new Pair<String, Object>(key, value), nanoTime));
	}

	public void putNoZoneConfigIDs(ArrayList<String> zids) {
		for (String zid : zids) {
			putNoZoneConfigID(zid, zid);
		}
	}

	/**
	 * ����BeaconConfig
	 * 
	 * @param
	 * @return
	 * @date 2014��3��18��
	 * @author trs
	 */
	public int cacheBeaconConfig(ArrayList<BeaconConfig> beaconConfigs) {

		String id = null;
		int count = 0;
		for (BeaconConfig beaconConfig : beaconConfigs) {

			id = beaconConfig._id;
			int add = putBeaconConfig(id, beaconConfig);
			count += add;
		}

		return count;
	}

	/**
	 * ��ȡFsm״̬��
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public Fsm getFsm(String key) {
		if (key == null) {
			return null;
		}
		return fsmMap.get(key);
	}

	/**
	 * ��ȡBeaconConfig
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public BeaconConfig getBeaconConfig(String key) {
		if (key == null) {
			return null;
		}
		return beaconConfigMap.get(key);
	}

	/**
	 * ��ȡZoneConfig
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public ZoneConfig getZoneConfig(String key) {
		if (key == null) {
			return null;
		}
		return zoneConfigMap.get(key);
	}

	/**
	 * ��ȡû��BeaconConfig��bleEvent�¼�
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public BleEvent getNoBeaconConfigBleEvent(String key) {
		if (key == null) {
			return null;
		}
		return noBeaconConfigBleEventMap.get(key);
	}

	/**
	 * ��ȡû��ZoneConfig��zid
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public String getNoZoneConfigID(String key) {
		if (key == null) {
			return null;
		}
		return noZoneConfigMap.get(key);
	}

	/**
	 * ��ȡ����ȫ����ʱ���еĸ���(�������)
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public long size() {
		return delayQueue.size();
	}

	/**
	 * ��ȡstore״̬���ĸ���
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public long sizeOfFsm() {
		return fsmMap.size();
	}

	/**
	 * ��ȡBeaconConfig�¼��ĸ���
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public long sizeOfBeaconConfig() {
		return beaconConfigMap.size();
	}

	/**
	 * ��ȡZoneConfig����
	 * 
	 * @param
	 * @return
	 * @date 2014��2��6��
	 * @author trs
	 */
	public long sizeOfZoneConfig() {
		return zoneConfigMap.size();
	}

	/**
	 * ɾ��һ��BeaconConfig
	 * 
	 * @param
	 * @return
	 * @date 2014��2��10��
	 * @author trs
	 */
	public BeaconConfig removeBeaconConfig(String key) {
		// ��BeaconConfig Map��ȡ��BeaconConfig
		Object object = beaconConfigMap.get(key);
		if (object == null) {
			return null;
		}
		BeaconConfig beaconConfig = (BeaconConfig) object;
		Pair<String, Object> pair = new Pair<String, Object>(key, beaconConfig);
		DelayItem<Pair<String, Object>> delayItem = new DelayItem<Pair<String, Object>>(
				pair, 0);
		delayQueue.remove(delayItem);
		BeaconConfig res = beaconConfigMap.remove(key);

		return res;
	}

	/**
	 * ɾ��һ��ZoneConfig
	 * 
	 * @param
	 * @return
	 * @date 2014��2��10��
	 * @author trs
	 */
	public ZoneConfig removeZoneConfig(String key) {
		// ��BeaconConfig Map��ȡ��BeaconConfig
		Object object = zoneConfigMap.get(key);
		if (object == null) {
			return null;
		}
		ZoneConfig zoneConfig = (ZoneConfig) object;
		Pair<String, Object> pair = new Pair<String, Object>(key, zoneConfig);
		DelayItem<Pair<String, Object>> delayItem = new DelayItem<Pair<String, Object>>(
				pair, 0);
		delayQueue.remove(delayItem);
		ZoneConfig res = zoneConfigMap.remove(key);

		return res;
	}

	/**
	 * ɾ��һ��Fsm
	 * 
	 * @param
	 * @return
	 * @date 2014��2��10��
	 * @author trs
	 */
	public Fsm removeFsm(String key) {
		// ��Fsm Map��ȡ��Fsm
		Object object = fsmMap.get(key);
		if (object == null) {
			return null;
		}
		Fsm fsm = (Fsm) object;
		Pair<String, Object> pair = new Pair<String, Object>(key, fsm);
		DelayItem<Pair<String, Object>> delayItem = new DelayItem<Pair<String, Object>>(
				pair, 0);
		delayQueue.remove(delayItem);
		Fsm res = fsmMap.remove(key);

		return res;
	}

	private void LoggerInfo(String info) {

		if (DEBUG) {
			Log.i(TAG, info);
		}
	}
}
