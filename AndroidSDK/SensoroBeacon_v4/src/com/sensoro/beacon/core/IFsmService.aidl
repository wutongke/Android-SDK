package com.sensoro.beacon.core;

import com.sensoro.beacon.core.Spot;
import com.sensoro.beacon.core.Zone;

interface IFsmService{  
	Spot[] getSpot();
	Zone[] getZones();
	void setUID(String uid);
	void setUUID(String uid);
	void initHeartBeat();
}