package com.sensoro.beacon.base.service;

import java.util.HashSet;
import java.util.Set;

import com.sensoro.beacon.base.IBeacon;

public class RangeState {
	private Callback callback;
	private Set<IBeacon> iBeacons = new HashSet<IBeacon>();
	
	public RangeState(Callback c) {
		callback = c;		
	}
	
	public Callback getCallback() {
		return callback;
	}
	public void clearIBeacons() {
		iBeacons.clear();
	}
	public Set<IBeacon> getIBeacons() {
		return iBeacons;
	}
	public void addIBeacon(IBeacon iBeacon) {
		iBeacons.add(iBeacon);
	}
	

}
