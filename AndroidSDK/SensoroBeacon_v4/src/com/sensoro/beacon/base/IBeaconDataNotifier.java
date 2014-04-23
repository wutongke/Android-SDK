package com.sensoro.beacon.base;

import com.sensoro.beacon.base.client.DataProviderException;

public interface IBeaconDataNotifier {
	public void iBeaconDataUpdate(IBeacon iBeacon, IBeaconData data, DataProviderException exception);
}
