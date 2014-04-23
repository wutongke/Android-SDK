package com.sensoro.beacon.base.client;

import android.os.Handler;

import com.sensoro.beacon.base.IBeacon;
import com.sensoro.beacon.base.IBeaconDataNotifier;

public class NullIBeaconDataFactory implements IBeaconDataFactory {

	@Override
	public void requestIBeaconData(IBeacon iBeacon, final IBeaconDataNotifier notifier) {	
		final Handler handler = new Handler();
		handler.post(new Runnable() {
			@Override
			public void run() {
				notifier.iBeaconDataUpdate(null, null, new DataProviderException("Please upgrade to the Pro version of the Android iBeacon Library."));
			}
		});		
	}
}

