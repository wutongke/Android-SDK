package com.sensoro.beacon.base;

/**
 * This interface is implemented by classes that receive iBeacon monitoring notifications
 * 
 * @see SensoroIBeaconManager#setMonitorNotifier(MonitorNotifier notifier)
 * @see SensoroIBeaconManager#startMonitoringBeaconsInRegion(Region region)
 * @see Region
 * 
 * @author tangrisheng
 */
public interface MonitorNotifier {
	/**
	 * Indicates the Android device is inside the Region of iBeacons
	 */
	public static final int INSIDE = 1;
	/**
	 * Indicates the Android device is outside the Region of iBeacons
	 */
	public static final int OUTSIDE = 0;
	
	/**
	 * Called when at least one iBeacon in a <code>Region</code> is visible.
	 * @param region a Region that defines the criteria of iBeacons to look for
	 */
	public void didEnterRegion(Region region);

	/**
	 * Called when no iBeacons in a <code>Region</code> are visible.
	 * @param region a Region that defines the criteria of iBeacons to look for
	 */
	public void didExitRegion(Region region);
	
	/**
	 * Called with a state value of MonitorNotifier.INSIDE when at least one iBeacon in a <code>Region</code> is visible.
	 * Called with a state value of MonitorNotifier.OUTSIDE when no iBeacons in a <code>Region</code> are visible.
	 * @param state either MonitorNotifier.INSIDE or MonitorNotifier.OUTSIDE
	 * @param region a Region that defines the criteria of iBeacons to look for
	 */
	public void didDetermineStateForRegion(int state, Region region);
}
