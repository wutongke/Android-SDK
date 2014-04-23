package com.sensoro.beacon.base;


import com.sensoro.beacon.base.service.IBeaconData;
import com.sensoro.beacon.base.service.MonitoringData;
import com.sensoro.beacon.base.service.RangingData;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class IBeaconIntentProcessor extends IntentService {
	private static final String TAG = "IBeaconIntentProcessor";

	public IBeaconIntentProcessor() {
		super("IBeaconIntentProcessor");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "got an intent to process");
		
		MonitoringData monitoringData = null;
		RangingData rangingData = null;
		
		if (intent != null && intent.getExtras() != null) {
			monitoringData = (MonitoringData) intent.getExtras().get("monitoringData");
			rangingData = (RangingData) intent.getExtras().get("rangingData");			
		}
		
		if (rangingData != null) {
			if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "got ranging data");
            if (rangingData.getIBeacons() == null) {
                Log.w(TAG, "Ranging data has a null iBeacons collection");
            }
			RangeNotifier notifier = SensoroIBeaconManager.getInstanceForApplication(this).getRangingNotifier();
			if (notifier != null) {
				notifier.didRangeBeaconsInRegion(IBeaconData.fromIBeaconDatas(rangingData.getIBeacons()), rangingData.getRegion());
			}
            else {
                if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "but ranging notifier is null, so we're dropping it.");
            }
		}
		if (monitoringData != null) {
			if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "got monitoring data");
			MonitorNotifier notifier = SensoroIBeaconManager.getInstanceForApplication(this).getMonitoringNotifier();
			if (notifier != null) {
				if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "Calling monitoring notifier:"+notifier);
				notifier.didDetermineStateForRegion(monitoringData.isInside() ? MonitorNotifier.INSIDE : MonitorNotifier.OUTSIDE, monitoringData.getRegion());
				if (monitoringData.isInside()) {
					notifier.didEnterRegion(monitoringData.getRegion());
				}
				else {
					notifier.didExitRegion(monitoringData.getRegion());					
				}
					
			}
		}
				
	}

}
