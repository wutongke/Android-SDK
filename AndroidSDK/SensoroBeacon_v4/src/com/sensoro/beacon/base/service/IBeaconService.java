package com.sensoro.beacon.base.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sensoro.beacon.base.IBeacon;
import com.sensoro.beacon.base.SensoroIBeaconManager;
import com.sensoro.beacon.base.Region;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class IBeaconService extends Service {
    public static final String TAG = "IBeaconService";

    private Map<Region, RangeState> rangedRegionState = new HashMap<Region, RangeState>();
    private Map<Region, MonitorState> monitoredRegionState = new HashMap<Region, MonitorState>();
    private BluetoothAdapter bluetoothAdapter;
    private boolean scanning;
    private boolean scanningPaused;
//    private Date lastIBeaconDetectionTime = new Date();
    private HashSet<IBeacon> trackedBeacons;
    private Handler handler = new Handler();
    private int bindCount = 0;

    private long scanPeriod = SensoroIBeaconManager.DEFAULT_FOREGROUND_SCAN_PERIOD;
    private long betweenScanPeriod = SensoroIBeaconManager.DEFAULT_FOREGROUND_BETWEEN_SCAN_PERIOD;

    private List<IBeacon> simulatedScanData = null;

    public class IBeaconBinder extends Binder {
        public IBeaconService getService() {
            Log.i(TAG, "getService of IBeaconBinder called");
            // Return this instance of LocalService so clients can call public methods
            return IBeaconService.this;
        }
    }


    /**
     * Command to the service to display a message
     */
    public static final int MSG_START_RANGING = 2;
    public static final int MSG_STOP_RANGING = 3;
    public static final int MSG_START_MONITORING = 4;
    public static final int MSG_STOP_MONITORING = 5;
    public static final int MSG_SET_SCAN_PERIODS = 6;


    static class IncomingHandler extends Handler {
        private final WeakReference<IBeaconService> mService;

        IncomingHandler(IBeaconService service) {
            mService = new WeakReference<IBeaconService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            IBeaconService service = mService.get();
            StartRMData startRMData = (StartRMData) msg.obj;

            if (service != null) {
                switch (msg.what) {
                    case MSG_START_RANGING:
                        Log.i(TAG, "start ranging received");
                        service.startRangingBeaconsInRegion(startRMData.getRegionData(), new com.sensoro.beacon.base.service.Callback(startRMData.getCallbackPackageName()));
                        service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod());
                        break;
                    case MSG_STOP_RANGING:
                        Log.i(TAG, "stop ranging received");
                        service.stopRangingBeaconsInRegion(startRMData.getRegionData());
                        service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod());
                        break;
                    case MSG_START_MONITORING:
                        Log.i(TAG, "start monitoring received");
                        service.startMonitoringBeaconsInRegion(startRMData.getRegionData(), new com.sensoro.beacon.base.service.Callback(startRMData.getCallbackPackageName()));
                        service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod());
                        break;
                    case MSG_STOP_MONITORING:
                        Log.i(TAG, "stop monitoring received");
                        service.stopMonitoringBeaconsInRegion(startRMData.getRegionData());
                        service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod());
                        break;
                    case MSG_SET_SCAN_PERIODS:
                        Log.i(TAG, "set scan intervals received");
                        service.setScanPeriods(startRMData.getScanPeriod(), startRMData.getBetweenScanPeriod());
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler(this));

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "binding");
        bindCount++;
        return mMessenger.getBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "unbinding");
        bindCount--;
        return false;
    }


    @Override
    public void onCreate() {
        Log.i(TAG, "iBeaconService is starting up");
        getBluetoothAdapter();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestory called.  stopping scanning");
        scanLeDevice(false);
        if (bluetoothAdapter != null) {
            bluetoothAdapter.stopLeScan(leScanCallback);
            lastScanEndTime = new Date().getTime();
        }
    }

    private int ongoing_notification_id = 1;

    /* 
     * Returns true if the service is running, but all bound clients have indicated they are in the background
     */
    private boolean isInBackground() {
        if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "bound client count:" + bindCount);
        return bindCount == 0;
    }

    /**
     * methods for clients
     */

    public void startRangingBeaconsInRegion(Region region, Callback callback) {
        if (rangedRegionState.containsKey(region)) {
            Log.i(TAG, "Already ranging that region -- will replace existing region.");
            rangedRegionState.remove(region); // need to remove it, otherwise the old object will be retained because they are .equal
        }
        rangedRegionState.put(region, new RangeState(callback));
        if (!scanning) {
            scanLeDevice(true);
        }
    }

    public void stopRangingBeaconsInRegion(Region region) {
        rangedRegionState.remove(region);
        if (scanning && rangedRegionState.size() == 0 && monitoredRegionState.size() == 0) {
            scanLeDevice(false);
        }
    }

    public void startMonitoringBeaconsInRegion(Region region, Callback callback) {
        if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "startMonitoring called");
        if (monitoredRegionState.containsKey(region)) {
            Log.i(TAG, "Already monitoring that region -- will replace existing region monitor.");
            monitoredRegionState.remove(region); // need to remove it, otherwise the old object will be retained because they are .equal
        }
        monitoredRegionState.put(region, new MonitorState(callback));
        if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "Currently monitoring " + monitoredRegionState.size() + " regions.");
        if (!scanning) {
            scanLeDevice(true);
        }

    }

    public void stopMonitoringBeaconsInRegion(Region region) {
        if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "stopMonitoring called");
        monitoredRegionState.remove(region);
        if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "Currently monitoring " + monitoredRegionState.size() + " regions.");
        if (scanning && rangedRegionState.size() == 0 && monitoredRegionState.size() == 0) {
            scanLeDevice(false);
        }
    }

    public void setScanPeriods(long scanPeriod, long betweenScanPeriod) {
        this.scanPeriod = scanPeriod;
        this.betweenScanPeriod = betweenScanPeriod;
        long now = new Date().getTime();
        if (nextScanStartTime > now) {
            long proposedNextScanStartTime = (lastScanEndTime + betweenScanPeriod);
            if (proposedNextScanStartTime < nextScanStartTime) {
                nextScanStartTime = proposedNextScanStartTime;
                Log.i(TAG, "Adjusted nextScanStartTime to be "+new Date(nextScanStartTime));
            }
        }
        if (scanStopTime > now) {
            long proposedScanStopTime = (lastScanStartTime + scanPeriod);
            if (proposedScanStopTime < scanStopTime) {
                scanStopTime = proposedScanStopTime;
                Log.i(TAG, "Adjusted scanStopTime to be "+new Date(scanStopTime));
            }
        }
    }

    private long lastScanStartTime = 0l;
    private long lastScanEndTime = 0l;
    private long nextScanStartTime = 0l;
    private long scanStopTime = 0l;

    private void scanLeDevice(final Boolean enable) {
        if (getBluetoothAdapter() == null) {
            Log.e(TAG, "No bluetooth adapter.  iBeaconService cannot scan.");
            if (simulatedScanData == null) {
                Log.w(TAG, "exiting");
                return;
            } else {
                Log.w(TAG, "proceeding with simulated scan data");
            }
        }
        if (enable) {
            long millisecondsUntilStart = nextScanStartTime - (new Date().getTime());
            if (millisecondsUntilStart > 0) {
                if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "Waiting to start next bluetooth scan for another "+millisecondsUntilStart+" milliseconds");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scanLeDevice(true);
                    }
                }, millisecondsUntilStart > 1000 ? 1000 : millisecondsUntilStart);
                return;
            }

            trackedBeacons = new HashSet<IBeacon>();
            if (scanning == false || scanningPaused == true) {
                scanning = true;
                scanningPaused = false;
                try {
                    if (getBluetoothAdapter() != null) {
                        if (getBluetoothAdapter().isEnabled()) {
                            getBluetoothAdapter().startLeScan(leScanCallback);
                            lastScanStartTime = new Date().getTime();
                        } else {
                            Log.w(TAG, "Bluetooth is disabled.  Cannot scan for iBeacons.");
                        }
                    }
                } catch (Exception e) {
                    Log.e("TAG", "Exception starting bluetooth scan.  Perhaps bluetooth is disabled or unavailable?");
                }
            } else {
                if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "We are already scanning");
            }
            scanStopTime = (new Date().getTime() + scanPeriod);
            scheduleScanStop();

            if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "Scan started");
        } else {
            if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "disabling scan");
            scanning = false;
            if (getBluetoothAdapter() != null) {
                getBluetoothAdapter().stopLeScan(leScanCallback);
                lastScanEndTime = new Date().getTime();
            }
        }
    }

    private void scheduleScanStop() {
        // Stops scanning after a pre-defined scan period.
        long millisecondsUntilStop = scanStopTime - (new Date().getTime());
        if (millisecondsUntilStop > 0) {
            if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "Waiting to stop scan for another "+millisecondsUntilStop+" milliseconds");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scheduleScanStop();
                }
            }, millisecondsUntilStop > 1000 ? 1000 : millisecondsUntilStop);
        }
        else {
            finishScanCycle();
        }


    }

    private void finishScanCycle() {
        if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "Done with scan cycle");
        processExpiredMonitors();
        if (scanning == true) {
            if (!anyRangingOrMonitoringRegionsActive()) {
                if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "Not starting scan because no monitoring or ranging regions are defined.");
            }
            else {
                processRangeData();
                if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "Restarting scan.  Unique beacons seen last cycle: " + trackedBeacons.size());
                if (getBluetoothAdapter() != null) {
                    if (getBluetoothAdapter().isEnabled()) {
                        getBluetoothAdapter().stopLeScan(leScanCallback);
                        lastScanEndTime = new Date().getTime();
                    } else {
                        Log.w(TAG, "Bluetooth is disabled.  Cannot scan for iBeacons.");
                    }
                }

                scanningPaused = true;
                // If we want to use simulated scanning data, do it here.  This is used for testing in an emulator
                if (simulatedScanData != null) {
                    if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                        for (IBeacon iBeacon : simulatedScanData) {
                            processIBeaconFromScan(iBeacon);
                        }
                    } else {
                        Log.w(TAG, "Simulated scan data provided, but ignored because we are not running in debug mode.  Please remove simulated scan data for production.");
                    }
                }
                nextScanStartTime = (new Date().getTime() + betweenScanPeriod);
                scanLeDevice(true);
            }
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     final byte[] scanRecord) {
                    if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "got record");
                    new ScanProcessor().execute(new ScanData(device, rssi, scanRecord));

                }
            };

    private class ScanData {
        public ScanData(BluetoothDevice device, int rssi, byte[] scanRecord) {
            this.device = device;
            this.rssi = rssi;
            this.scanRecord = scanRecord;
        }

        @SuppressWarnings("unused")
        public BluetoothDevice device;
        public int rssi;
        public byte[] scanRecord;
    }

    private void processRangeData() {
        Iterator<Region> regionIterator = rangedRegionState.keySet().iterator();
        while (regionIterator.hasNext()) {
            Region region = regionIterator.next();
            RangeState rangeState = rangedRegionState.get(region);
            if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "Calling ranging callback with " + rangeState.getIBeacons().size() + " iBeacons");
            rangeState.getCallback().call(IBeaconService.this, "rangingData", new RangingData(rangeState.getIBeacons(), region));
            rangeState.clearIBeacons();
        }

    }

    private void processExpiredMonitors() {
        Iterator<Region> monitoredRegionIterator = monitoredRegionState.keySet().iterator();
        while (monitoredRegionIterator.hasNext()) {
            Region region = monitoredRegionIterator.next();
            MonitorState state = monitoredRegionState.get(region);
            if (state.isNewlyOutside()) {
                if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "found a monitor that expired: " + region);
                state.getCallback().call(IBeaconService.this, "monitoringData", new MonitoringData(state.isInside(), region));
            }
        }
    }

    private void processIBeaconFromScan(IBeacon iBeacon) {
//        lastIBeaconDetectionTime = new Date();
        trackedBeacons.add(iBeacon);
        if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG,
                "iBeacon detected :" + iBeacon.getProximityUuid() + " "
                        + iBeacon.getMajor() + " " + iBeacon.getMinor()
                        + " accuracy: " + iBeacon.getAccuracy()
                        + " proximity: " + iBeacon.getProximity());

        List<Region> matchedRegions = matchingRegions(iBeacon,
                monitoredRegionState.keySet());
        Iterator<Region> matchedRegionIterator = matchedRegions.iterator();
        while (matchedRegionIterator.hasNext()) {
            Region region = matchedRegionIterator.next();
            MonitorState state = monitoredRegionState.get(region);
            if (state.markInside()) {
                state.getCallback().call(IBeaconService.this, "monitoringData",
                        new MonitoringData(state.isInside(), region));
            }
        }

        if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "looking for ranging region matches for this ibeacon");
        matchedRegions = matchingRegions(iBeacon, rangedRegionState.keySet());
        matchedRegionIterator = matchedRegions.iterator();
        while (matchedRegionIterator.hasNext()) {
            Region region = matchedRegionIterator.next();
            if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "matches ranging region: " + region);
            RangeState rangeState = rangedRegionState.get(region);
            rangeState.addIBeacon(iBeacon);
        }
    }

    private class ScanProcessor extends AsyncTask<ScanData, Void, Void> {

        @Override
        protected Void doInBackground(ScanData... params) {
            ScanData scanData = params[0];

            IBeacon iBeacon = IBeacon.fromScanData(scanData.scanRecord,
                    scanData.rssi);
            if (iBeacon != null) {
                processIBeaconFromScan(iBeacon);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private List<Region> matchingRegions(IBeacon iBeacon, Collection<Region> regions) {
        List<Region> matched = new ArrayList<Region>();
        Iterator<Region> regionIterator = regions.iterator();
        while (regionIterator.hasNext()) {
            Region region = regionIterator.next();
            if (region.matchesIBeacon(iBeacon)) {
                matched.add(region);
            } else {
                if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "This region does not match: " + region);
            }

        }
        return matched;
    }

    /*
     Returns false if no ranging or monitoring regions have beeen requested.  This is useful in determining if we should scan at all.
     */
    private boolean anyRangingOrMonitoringRegionsActive() {
        return (rangedRegionState.size() + monitoredRegionState.size()) > 0;
    }

    private BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            // Initializes Bluetooth adapter.
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) this.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        return bluetoothAdapter;
    }

}
