package com.sensoro.sdkdemo;

import java.util.Map;

import android.content.Intent;
import android.util.Log;

import com.sensoro.beacon.core.Action;
import com.sensoro.beacon.core.Beacon;
import com.sensoro.beacon.core.FsmService;
import com.sensoro.beacon.core.Spot;
import com.sensoro.beacon.core.Zone;

/**
 * @description APP�������Զ�����,�̳�FsmService.��FsmService���ֻص�������,����ͬ�Ĳ�ε�beacon�����¼�.
 * @date 2014-04-21
 * @author Sensoro
 */

public class SensoroFsmService extends FsmService {
	private static final String TAG = "SensoroFsmService";
	public static final String ENTER_SPOT = "enter_spot"; // SDK �ص�action��������.���SPOT����.

	public static final String TYPE = "type"; // �Զ���SDK server spot����param��key
	public static final String FIXEDCORNER = "fixedcorner"; // �Զ���SDK Server spot����param��,keyΪtype��ֵ
	public static final String MESSAGE = "message"; // �Զ���SDK Server action����param��key

	public static final String SENSORO_ACTION = "android.intent.action.SENSORO_ACTION"; // �Զ���㲥action����
	public static final String BROADCAST_NAME = "BROADCAST_NAME"; // �Զ���㲥��������
	public static final String ENTER_MESSAGE = "ENTER_MESSAGE"; // �Զ���㲥���ݵĽ�����Ϣ
	public static final String ENTER_FIXCORNER = "ENTER_FIXCORNER"; // �Զ���㲥���ݵĽ���ҡһҡ����
	public static final String LEAVE_FIXCORNER = "LEAVE_FIXCORNER"; // �Զ���㲥���ݵ��뿪ҡһҡ����

	@Override
	public void onNew(Beacon beacon) {
		Log.v(TAG,
				"onNew--" + "major" + beacon.getMajor() + "minor"
						+ beacon.getMinor());
	}

	@Override
	public void onGone(Beacon beacon) {
		Log.v(TAG,
				"onGone--" + "major" + beacon.getMajor() + "minor"
						+ beacon.getMinor());
	}

	/**
	 * Description: ����һ��Ԥ����ĵ�ʱ�ص��÷���.
	 * 
	 * @param Spot spot {@link Spot}
	 * @param Zone zone {@link Zone}
	 * @return null
	 * 
	 * @example demo��Spot��param����,�Զ������keyΪtype,type����ֵΪfixedcorner��ʾ����ҡһҡ����.
	 * 			������������,�򹹳ɽ���ҡһҡ�����ҵ��.
	 * 			demo������ҡһҡ����Ϊһ��spot����,������Ҳ�����Զ�����һ��Zone������.
	 * 
	 * */
	@Override
	public void onEnterSpot(Spot spot, Zone zone) {
		Map<String, String> param = spot.getParam();
		if (param != null) { // ��������SDK server��û���ÿ���Ϊ��
			String type = param.get(TYPE);
			if (type != null && type.equals(FIXEDCORNER)) {
				// ��������typeΪfixcorner --> ����Ϊ����ҡһҡ����
				// TODO ����Ϊ���������д���,demo��ͨ���㲥���͸�activity��ʾ

				Intent intent = new Intent();
				intent.putExtra(BROADCAST_NAME, ENTER_FIXCORNER);
				intent.setAction(SENSORO_ACTION);
				sendBroadcast(intent);
			}
		}
	}

	/**
	 * Description: �뿪һ��Ԥ����ĵ�ʱ�ص��÷���.
	 * 
	 * @param Spot spot {@link Spot}
	 * @param Zone zone {@link Zone}
	 * @return null
	 * 
	 * @example demo��Spot��param����,�Զ������keyΪtype,type����ֵΪfixedcorner��ʾ�뿪ҡһҡ����.
	 * 			������������,�򹹳��뿪ҡһҡ�����ҵ��.
	 * 			demo������ҡһҡ����Ϊһ��spot����,������Ҳ�����Զ�����һ��Zone������.
	 * 
	 * */
	@Override
	public void onLeaveSpot(Spot spot, Zone zone) {
		Map<String, String> param = spot.getParam();
		if (param != null) { // ��������SDK server��û���ÿ���Ϊ��
			String type = param.get(TYPE);
			if (type != null && type.equals(FIXEDCORNER)) {
				// ��������typeΪfixedcorner --> ����Ϊ�뿪ҡһҡ����
				// TODO ����Ϊ���������д���,demo��ͨ���㲥���͸�activity��ʾ

				Intent intent = new Intent();
				intent.putExtra(BROADCAST_NAME, LEAVE_FIXCORNER);
				intent.setAction(SENSORO_ACTION);
				sendBroadcast(intent);
			}
		}
	}

	@Override
	public void onStaySpot(Spot spot, Zone zone, long seconds) {
		Log.v(TAG, "onStaySpot--" + spot.toString());
	}

	@Override
	public void onEnterZone(Zone zone, Spot spot) {
		Log.v(TAG, "onEnterZone--" + zone.toString());
	}

	@Override
	public void onLeaveZone(Zone zone, Spot spot) {
		Log.v(TAG, "onLeaveZone--" + zone.toString());
	}

	@Override
	public void onStayZone(Zone zone, Spot spot, long seconds) {
		Log.v(TAG, "onStayZone--" + zone.toString());
	}

	/**
	 * Description:	����Ԥ����Ľ�������ý��
	 * 
	 * @param Action {@link Action}
	 * @return null
	 * 
	 * @example demo��Action��param����,�Զ��������keyΪmessage,�ǿձ�ʾ�н�����Ϣ;
	 * 			action��ֵΪenter_spot,��ʾ����ĳ����.����������������,�򹹳��û�����ҵ��.
	 * 
	 */
	@Override
	public void onAction(Action action) {
		Map<String, String> param = action.getParam();
		String message = null;
		if (param != null) { // ��������SDK server��û���ÿ���Ϊ��
			message = param.get(MESSAGE);
		}
		String act = action.getAction();
		if (act.equals(ENTER_SPOT)) {
			// ����ĳ����
			if (message != null) {
				// ��������Ϣ�ǿ� --> ����Ϊ������Ϣ
				// TODO ����Ϊ���������д���,demo��ͨ���㲥���͸�activity��ʾ

				Intent intent = new Intent();
				intent.putExtra(BROADCAST_NAME, ENTER_MESSAGE);
				intent.putExtra(ENTER_MESSAGE, message);
				intent.setAction(SENSORO_ACTION);
				sendBroadcast(intent);
			}
		}
	}
}
