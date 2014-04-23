package com.sensoro.beacon.core;

/**
 * 对应action_record表记录
 * 
 * @date 2014年4月16日
 */
class ActionRecord {
	int _id; // 自增长的id
	String id; // 触发action的id,可能是BeaconConfig或者ZoneConfig的id
	int type; // spot或者zone
	int name; // enter, leave, stay
	long timestamp; // 发出action的时间
	int times; // 触发action的次数
}
