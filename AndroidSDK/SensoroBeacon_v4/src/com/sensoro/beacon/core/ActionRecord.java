package com.sensoro.beacon.core;

/**
 * ��Ӧaction_record���¼
 * 
 * @date 2014��4��16��
 */
class ActionRecord {
	int _id; // ��������id
	String id; // ����action��id,������BeaconConfig����ZoneConfig��id
	int type; // spot����zone
	int name; // enter, leave, stay
	long timestamp; // ����action��ʱ��
	int times; // ����action�Ĵ���
}
