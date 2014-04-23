package com.sensoro.beacon.base.client;

import com.sensoro.beacon.base.IBeacon;
import com.sensoro.beacon.base.IBeaconDataNotifier;
public interface IBeaconDataFactory {
	public void requestIBeaconData(IBeacon iBeacon, IBeaconDataNotifier notifier);
}
	
