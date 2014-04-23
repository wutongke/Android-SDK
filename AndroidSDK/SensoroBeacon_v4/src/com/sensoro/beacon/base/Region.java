package com.sensoro.beacon.base;

import android.util.Log;

public class Region  {
	private static final String TAG = "Region";
	/**
	 * Part 2 of 3 of an iBeacon identifier.  A 16 bit integer typically used to identify a common grouping of iBeacons.
	 */
	protected Integer major;
	/**
	 * Part 3 of 3 of an iBeacon identifier.  A 16 bit integer typically used to identify an individual iBeacon within a group.
	 */
	protected Integer minor;
	/**
	 * Part 1 of 3 of an iBeacon identifier.  A 26 byte UUID typically used to identify the company that owns a set of iBeacons.
	 */
	protected String proximityUuid;
	/**
	 * A unique identifier used to later cancel Ranging and Monitoring, or change the region being Ranged/Monitored
	 */
	protected String uniqueId;
	/**
	 * Constructs a new Region object to be used for Ranging or Monitoring
	 * @param uniqueId - A unique identifier used to later cancel Ranging and Monitoring, or change the region being Ranged/Monitored
	 * @param proximityUuid
	 * @param major
	 * @param minor
	 */
	public Region(String uniqueId, String proximityUuid, Integer major, Integer minor) {
		this.major = major;
		this.minor = minor;
		this.proximityUuid = normalizeProximityUuid(proximityUuid);
		this.uniqueId = uniqueId;
        if (uniqueId == null) {
            throw new NullPointerException("uniqueId may not be null");
        }
	}
	/**
	 * @see #major
	 * @return major
	 */
	public Integer getMajor() {
		return major;
	}
	/**
	 * @see #minor
	 * @return minor
	 */
	public Integer getMinor() {
		return minor;
	}
	/**
	 * @see #proximityUuid
	 * @return proximityUuid
	 */

	public String getProximityUuid() {
		return proximityUuid;
	}
	/**
	 * @see #uniqueId
	 * @return uniqueId
	 */
	public String getUniqueId() {
		return uniqueId;
	}
	
	/**
	 * Checks to see if an IBeacon object is included in the matching criteria of this Region
	 * @param iBeacon the iBeacon to check to see if it is in the Region
	 * @return true if is covered
	 */
	public boolean matchesIBeacon(IBeacon iBeacon) {
		if (proximityUuid != null && !iBeacon.getProximityUuid().equals(proximityUuid)) {
			if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "unmatching proxmityUuids: "+iBeacon.getProximityUuid()+" != "+proximityUuid);
			return false;
		}
		if (major != null && iBeacon.getMajor() != major) {
			if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "unmatching major: "+iBeacon.getMajor()+" != "+major);
			return false;
		}
		if (minor != null && iBeacon.getMinor() != minor) {
			if (SensoroIBeaconManager.LOG_DEBUG) Log.d(TAG, "unmatching minor: "+iBeacon.getMajor()+" != "+minor);
			return false;
		}
		return true;
	}
	
	protected Region(Region otherRegion) {
		major = otherRegion.major;
		minor = otherRegion.minor;
		proximityUuid = otherRegion.proximityUuid;
		uniqueId = otherRegion.uniqueId;
	}
	protected Region() {
		
	}

	@Override
	public int hashCode() {
		return this.uniqueId.hashCode();
	}
	
	public boolean equals(Object other) {
		 if (other instanceof Region) {
			return ((Region)other).uniqueId.equals(this.uniqueId);
		 }
		 return false;
	}
	
	public String toString() {
		return "proximityUuid: "+proximityUuid+" major: "+major+" minor:"+minor;
	}
	
	/**
	 * Puts string to a normalized UUID format, or throws a runtime exception if it contains non-hex digits
	 * other than dashes or spaces, or if it doesn't contain exactly 32 hex digits
	 * @param proximityUuid uuid with any combination of upper/lower case hex characters, dashes and spaces
	 * @return a normalized string, all lower case hex characters with dashes in the form e2c56db5-dffb-48d2-b060-d0f5a71096e0
	 */
	public static String normalizeProximityUuid(String proximityUuid) {
		if (proximityUuid == null) {
			return null;			
		}
		String dashlessUuid = proximityUuid.toLowerCase().replaceAll("[\\-\\s]", "");
		if (dashlessUuid.length() != 32) {
			// TODO: make this a specific exception
			throw new RuntimeException("UUID: "+proximityUuid+" is too short.  Must contain exactly 32 hex digits, and there are this value has "+dashlessUuid.length()+" digits.");
		}
		if (!dashlessUuid.matches("^[a-fA-F0-9]*$")) {
			// TODO: make this a specific exception
			throw new RuntimeException("UUID: "+proximityUuid+" contains invalid characters.  Must be dashes, a-f and 0-9 characters only.");			
		}
		StringBuilder sb = new StringBuilder();
		sb.append(dashlessUuid.substring(0,8));
		sb.append('-');
		sb.append(dashlessUuid.substring(8,12));
		sb.append('-');
		sb.append(dashlessUuid.substring(12,16));
		sb.append('-');
		sb.append(dashlessUuid.substring(16,20));
		sb.append('-');
		sb.append(dashlessUuid.substring(20,32));
		return sb.toString();
	}



}
