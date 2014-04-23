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

	// 状态机容器
	ConcurrentHashMap<String, Fsm> fsmMap; // 状态机
	// bleEvent时间队列容器
	ConcurrentHashMap<String, BeaconConfig> beaconConfigMap; // BeaconConfig的缓存
	ConcurrentHashMap<String, ZoneConfig> zoneConfigMap; // BeaconConfig的缓存
	ConcurrentHashMap<String, BleEvent> noBeaconConfigBleEventMap; // 获取不到beaconConfig的BleEvent的缓存,有效时间min
	ConcurrentHashMap<String, String> noZoneConfigMap; // 获取不到ZoneConfig的缓存,有效时间min
	private DelayQueue<DelayItem<Pair<String, Object>>> delayQueue; // 用于控制超时的DelayQueue,同时控制状态机和缓存的超时

	private Thread daemonThread; // 守护进程

	public Cache() {

		InitContainer();
		InitDaemonThread();
	}

	private void daemonCheck() {

		LoggerInfo("cache service started");
		while (true) {
			try {
				// 获取超时
				// in和out的的状态机如果超时则丢弃,其他的状态机则更新回对应conf未触发的状态
				DelayItem<Pair<String, Object>> delayItem = delayQueue.take();
				if (delayItem != null) {
					// 超时对象处理
					// 根据超时删除对应的状态机
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
		// 守护进程,用于状态机超时处理
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
	 * 超时各个容器
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
	 * @author trs
	 */
	private void InitContainer() {
		// 状态机
		fsmMap = new ConcurrentHashMap<String, Fsm>();
		// beaconConfig事件
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
	 * 添加Store状态机,可以控制超时时间
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
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
	 * 添加BleEvent事件,可以控制超时时间
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
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
	 * 添加BleEvent事件,可以控制超时时间
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
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
	 * 添加Store状态机,使用默认超时时间
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
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
		// 默认的store超时
		long nanoTime = TimeUnit.NANOSECONDS.convert(
				SensoroSense.FSM_TIMEOUT_DEFAULT, TimeUnit.MILLISECONDS);
		delayQueue.put(new DelayItem<Pair<String, Object>>(
				new Pair<String, Object>(key, value), nanoTime));
	}

	/**
	 * 添加BeaconConfig
	 * 
	 * @param
	 * @return BeaconConfig缓存增加的个数
	 * @date 2014年2月6日
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
	 * 添加BeaconConfig
	 * 
	 * @param
	 * @return BeaconConfig缓存增加的个数
	 * @date 2014年2月6日
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
	 * 缓存BeaconConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年3月18日
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
	 * 获取Fsm状态机
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
	 * @author trs
	 */
	public Fsm getFsm(String key) {
		if (key == null) {
			return null;
		}
		return fsmMap.get(key);
	}

	/**
	 * 获取BeaconConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
	 * @author trs
	 */
	public BeaconConfig getBeaconConfig(String key) {
		if (key == null) {
			return null;
		}
		return beaconConfigMap.get(key);
	}

	/**
	 * 获取ZoneConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
	 * @author trs
	 */
	public ZoneConfig getZoneConfig(String key) {
		if (key == null) {
			return null;
		}
		return zoneConfigMap.get(key);
	}

	/**
	 * 获取没有BeaconConfig的bleEvent事件
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
	 * @author trs
	 */
	public BleEvent getNoBeaconConfigBleEvent(String key) {
		if (key == null) {
			return null;
		}
		return noBeaconConfigBleEventMap.get(key);
	}

	/**
	 * 获取没有ZoneConfig的zid
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
	 * @author trs
	 */
	public String getNoZoneConfigID(String key) {
		if (key == null) {
			return null;
		}
		return noZoneConfigMap.get(key);
	}

	/**
	 * 获取现有全部超时队列的个数(会有误差)
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
	 * @author trs
	 */
	public long size() {
		return delayQueue.size();
	}

	/**
	 * 获取store状态机的个数
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
	 * @author trs
	 */
	public long sizeOfFsm() {
		return fsmMap.size();
	}

	/**
	 * 获取BeaconConfig事件的个数
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
	 * @author trs
	 */
	public long sizeOfBeaconConfig() {
		return beaconConfigMap.size();
	}

	/**
	 * 获取ZoneConfig个数
	 * 
	 * @param
	 * @return
	 * @date 2014年2月6日
	 * @author trs
	 */
	public long sizeOfZoneConfig() {
		return zoneConfigMap.size();
	}

	/**
	 * 删除一个BeaconConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年2月10日
	 * @author trs
	 */
	public BeaconConfig removeBeaconConfig(String key) {
		// 从BeaconConfig Map中取得BeaconConfig
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
	 * 删除一个ZoneConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年2月10日
	 * @author trs
	 */
	public ZoneConfig removeZoneConfig(String key) {
		// 从BeaconConfig Map中取得BeaconConfig
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
	 * 删除一个Fsm
	 * 
	 * @param
	 * @return
	 * @date 2014年2月10日
	 * @author trs
	 */
	public Fsm removeFsm(String key) {
		// 从Fsm Map中取得Fsm
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
