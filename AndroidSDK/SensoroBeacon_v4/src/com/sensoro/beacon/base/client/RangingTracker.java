package com.sensoro.beacon.base.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.util.Log;

import com.sensoro.beacon.base.IBeacon;
import com.sensoro.beacon.base.SensoroIBeaconManager;

public class RangingTracker {
	private static String TAG = "RangingTracker";	
	private Map<IBeacon,RangedIBeacon> rangedIBeacons = new HashMap<IBeacon,RangedIBeacon>();
	public void addIBeacon(IBeacon iBeacon) {
		if (rangedIBeacons.containsKey(iBeacon)) {
			RangedIBeacon rangedIBeacon = rangedIBeacons.get(iBeacon);
			if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "adding "+iBeacon.getProximityUuid()+" to existing range for: "+rangedIBeacon.getProximityUuid() );
			rangedIBeacon.addRangeMeasurement(iBeacon.getRssi());
		}
		else {
			if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "adding "+iBeacon.getProximityUuid()+" to new rangedIBeacon");
			rangedIBeacons.put(iBeacon, new RangedIBeacon(iBeacon));
		}
	}
	public synchronized Collection<IBeacon> getIBeacons() {	
		ArrayList<IBeacon> iBeacons = new ArrayList<IBeacon>();		
		Iterator<RangedIBeacon> iterator = rangedIBeacons.values().iterator();
		while (iterator.hasNext()) {
			RangedIBeacon rangedIBeacon = iterator.next();
			if (!rangedIBeacon.allMeasurementsExpired()) {
				iBeacons.add(rangedIBeacon);				
			}
		}
		return iBeacons;
	}
	

}
