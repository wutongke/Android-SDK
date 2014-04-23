package com.sensoro.beacon.base;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.sensoro.beacon.base.client.RangingTracker;
import com.sensoro.beacon.base.service.IBeaconService;
import com.sensoro.beacon.base.service.RegionData;
import com.sensoro.beacon.base.service.StartRMData;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * @author tangrisheng
 */
public class SensoroIBeaconManager {
	private static final String TAG = "IBeaconManager";
	private Context context;
	protected static SensoroIBeaconManager client = null;
	private Map<IBeaconConsumer,ConsumerInfo> consumers = new HashMap<IBeaconConsumer,ConsumerInfo>();
	private Messenger serviceMessenger = null;
	protected RangeNotifier rangeNotifier = null;
    protected MonitorNotifier monitorNotifier = null;
    protected RangingTracker rangingTracker = new RangingTracker();
    public static boolean LOG_DEBUG = false;


    public static final long DEFAULT_FOREGROUND_SCAN_PERIOD = 1100;
    public static final long DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD = 0;
    public static final long DEFAULT_BACKGROUND_SCAN_PERIOD = 10000;
    public static final long DEFAULT_BACKGROUND_BETWEEN_SCAN_PERIOD = 5*60*1000;

    private long foregroundScanPeriod = DEFAULT_FOREGROUND_SCAN_PERIOD;
    private long foregroundBetweenScanPeriod = DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD;
    private long backgroundScanPeriod = DEFAULT_BACKGROUND_SCAN_PERIOD;
    private long backgroundBetweenScanPeriod = DEFAULT_BACKGROUND_BETWEEN_SCAN_PERIOD;

    public void setForegroundScanPeriod(long p) {
        foregroundBetweenScanPeriod = p;
    }
    public void setForegroundBetweenScanPeriod(long p) {
        foregroundBetweenScanPeriod = p;
    }
    public void setBackgroundScanPeriod(long p) {
        backgroundScanPeriod = p;
    }
    public void setBackgroundBetweenScanPeriod(long p) {
        backgroundBetweenScanPeriod = p;
    }

	public static SensoroIBeaconManager getInstanceForApplication(Context context) {
		if (client == null) {
			if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "IBeaconManager instance creation");
			client = new SensoroIBeaconManager(context);
		}
		return client;
	}
	
	protected SensoroIBeaconManager(Context context) {
		this.context = context;
	}
	public boolean checkAvailability() {
		if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			throw new BleNotAvailableException("Bluetooth LE not supported by this device"); 
		}		
		else {
			if (((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled()){
				return true;
			}
		}	
		return false;
	}
	public void bind(IBeaconConsumer consumer) {
		if (consumers.keySet().contains(consumer)) {
			if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "This consumer is already bound");
		}
		else {
			Log.d(TAG, "This consumer is not bound.  binding: "+consumer);
			consumers.put(consumer, new ConsumerInfo());
			Intent intent = new Intent(consumer.getApplicationContext(), IBeaconService.class);
			consumer.bindService(intent, iBeaconServiceConnection, Context.BIND_AUTO_CREATE);
			if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "consumer count is now:"+consumers.size());
            if (serviceMessenger != null) { // If the serviceMessenger is not null, that means we are able to make calls to the service
                setBackgroundMode(consumer, false); // if we just bound, we assume we are not in the background.
            }
 		}
	}
	
	public void unBind(IBeaconConsumer consumer) {
		if (consumers.keySet().contains(consumer)) {
			Log.d(TAG, "Unbinding");
			consumer.unbindService(iBeaconServiceConnection);
			consumers.remove(consumer);
		}
		else {
			if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "This consumer is not bound to: "+consumer);
			if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "Bound consumers: ");
			for (int i = 0; i < consumers.size(); i++) {
				Log.i(TAG, " "+consumers.get(i));
			}
		}
	}

    public boolean isBound(IBeaconConsumer consumer) {
        return consumers.keySet().contains(consumer) && (serviceMessenger != null);
    }

    public boolean setBackgroundMode(IBeaconConsumer consumer, boolean backgroundMode) {
        try {
            ConsumerInfo consumerInfo = consumers.get(consumer);
            consumerInfo.isInBackground = backgroundMode;
            consumers.put(consumer,consumerInfo);
            setScanPeriods();
            return true;
        }
        catch (RemoteException e) {
            Log.e(TAG, "Failed to set background mode", e);
            return false;
        }
    }

	public void setRangeNotifier(RangeNotifier notifier) {
		rangeNotifier = notifier;
	}

	public void setMonitorNotifier(MonitorNotifier notifier) {
		monitorNotifier = notifier;
	}
	
	public void startRangingBeaconsInRegion(Region region) throws RemoteException {
        if (serviceMessenger == null) {
            throw new RemoteException("The IBeaconManager is not bound to the service.  Call iBeaconManager.bind(IBeaconConsumer consumer) and wait for a callback to onIBeaconServiceConnect()");
        }
		Message msg = Message.obtain(null, IBeaconService.MSG_START_RANGING, 0, 0);
		StartRMData obj = new StartRMData(new RegionData(region), callbackPackageName(), this.getScanPeriod(), this.getBetweenScanPeriod() );
		msg.obj = obj;
		serviceMessenger.send(msg);
	}
	public void stopRangingBeaconsInRegion(Region region) throws RemoteException {
        if (serviceMessenger == null) {
            throw new RemoteException("The IBeaconManager is not bound to the service.  Call iBeaconManager.bind(IBeaconConsumer consumer) and wait for a callback to onIBeaconServiceConnect()");
        }
		Message msg = Message.obtain(null, IBeaconService.MSG_STOP_RANGING, 0, 0);
		StartRMData obj = new StartRMData(new RegionData(region), callbackPackageName(),this.getScanPeriod(), this.getBetweenScanPeriod() );
		msg.obj = obj;
		serviceMessenger.send(msg);
	}
	public void startMonitoringBeaconsInRegion(Region region) throws RemoteException {
        if (serviceMessenger == null) {
            throw new RemoteException("The IBeaconManager is not bound to the service.  Call iBeaconManager.bind(IBeaconConsumer consumer) and wait for a callback to onIBeaconServiceConnect()");
        }
		Message msg = Message.obtain(null, IBeaconService.MSG_START_MONITORING, 0, 0);
		StartRMData obj = new StartRMData(new RegionData(region), callbackPackageName(),this.getScanPeriod(), this.getBetweenScanPeriod()  );
		msg.obj = obj;
		serviceMessenger.send(msg);
	}
	public void stopMonitoringBeaconsInRegion(Region region) throws RemoteException {
        if (serviceMessenger == null) {
            throw new RemoteException("The IBeaconManager is not bound to the service.  Call iBeaconManager.bind(IBeaconConsumer consumer) and wait for a callback to onIBeaconServiceConnect()");
        }
		Message msg = Message.obtain(null, IBeaconService.MSG_STOP_MONITORING, 0, 0);
		StartRMData obj = new StartRMData(new RegionData(region), callbackPackageName(),this.getScanPeriod(), this.getBetweenScanPeriod() );
		msg.obj = obj;
		serviceMessenger.send(msg);
	}

    public void setScanPeriods() throws RemoteException {
        if (serviceMessenger == null) {
            throw new RemoteException("The IBeaconManager is not bound to the service.  Call iBeaconManager.bind(IBeaconConsumer consumer) and wait for a callback to onIBeaconServiceConnect()");
        }
        Message msg = Message.obtain(null, IBeaconService.MSG_SET_SCAN_PERIODS, 0, 0);
        StartRMData obj = new StartRMData(this.getScanPeriod(), this.getBetweenScanPeriod());
        msg.obj = obj;
        serviceMessenger.send(msg);
    }
	
	private String callbackPackageName() {
		String packageName = context.getPackageName();
		if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "callback packageName: "+packageName);
		return packageName;
	}

	private ServiceConnection iBeaconServiceConnection = new ServiceConnection() {
		// Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        serviceMessenger = new Messenger(service);
	        Iterator<IBeaconConsumer> consumerIterator = consumers.keySet().iterator();
	        while (consumerIterator.hasNext()) {
	        	IBeaconConsumer consumer = consumerIterator.next();
	        	Boolean alreadyConnected = consumers.get(consumer).isConnected;
	        	if (!alreadyConnected) {		        	
		        	consumer.onIBeaconServiceConnect();
                    ConsumerInfo consumerInfo = consumers.get(consumer);
                    consumerInfo.isConnected = true;
		        	consumers.put(consumer,consumerInfo);
	        	}
	        }
	    }

	    // Called when the connection with the service disconnects unexpectedly
	    public void onServiceDisconnected(ComponentName className) {
	        Log.e(TAG, "onServiceDisconnected");
	    }
	};	

	public MonitorNotifier getMonitoringNotifier() {
		return this.monitorNotifier;		
	}	
	public RangeNotifier getRangingNotifier() {
		return this.rangeNotifier;		
	}

    private class ConsumerInfo {
        public boolean isConnected = false;
        public boolean isInBackground = false;
    }

    private boolean isInBackground() {
        boolean background = true;
        for (IBeaconConsumer consumer : consumers.keySet()) {
            if (!consumers.get(consumer).isInBackground) {
                background = false;
            }
        }
        return background;
    }

    private long getScanPeriod() {
        if (isInBackground()) {
            return backgroundScanPeriod;
        }
        else {
            return foregroundScanPeriod;
        }
    }
    private long getBetweenScanPeriod() {
        if (isInBackground()) {
            return backgroundBetweenScanPeriod;
        }
        else {
            return foregroundBetweenScanPeriod;
        }
    }

}
