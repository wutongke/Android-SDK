package com.sensoro.beacon.base;

import java.util.Collection;
/**
 * @author tangrisheng
 */
public interface RangeNotifier {
	public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region);
}
