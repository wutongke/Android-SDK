package com.sensoro.sdkdemo;

import com.sensoro.beacon.core.SensoroSense;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;

public class SensoroSDKDemoApplication extends Application {
	private static final String APP_ID = "3";
	private static final String APP_KEY = null;
	// ����������
	private BluetoothAdapter bluetoothAdapter = null;
	// �Ƿ�֧��BLE��־λ
	public boolean isSupportBLE = false;
	// �Ƿ�������
	public boolean isBTOpen = false;
	// ������¼�������
	public SensoroSense sensoroSense = null;

	@Override
	public void onCreate() {
		initBluetooth();
		startFsmService();
		super.onCreate();
	}

	private void startFsmService() {
		/**
		 * ֻ���豸֧��BLE������������,���ܱ�֤SDK����ʹ��.
		 * �û��������д���.
		 */
		if (isSupportBLE && isBTOpen) {
			/**
			 * ��ȡSensoroSense��ʵ��. ��һ������Ϊcontext;
			 * �ڶ���������APP_ID,��ͬAPP��Ҫ��Sensoro��ѯ���벻ͬ��APP_ID;
			 * ������������APP_KEY,��ͬAPP��Ҫ��ͬ��APP_KEY.�ò�����δʹ��.
			 * eg:	demo�����ò���APP_IDΪ1,APP_KEYΪnull.
			 * 
			 */
			sensoroSense = SensoroSense.getIntance(this, APP_ID, APP_KEY);
			/**
			 * �����û��Զ���Service,��ȡBeacon�����Ļص�����. ��һ������ΪContext;
			 * �ڶ�������intent��ʾ�����û��Զ���service��intent; 
			 * ������������ʾ������̹ر��Ժ��Ƿ�������������
			 * ���ĸ�����ΪMap���ɵ�Log��ѡ��,Ĭ��log������Ϊtrue,�����������£�
			 * 	options { 
			 * 		// ��ѡ�������� 
			 * 		bool local_log,��ʾ����log�洢
			 * 		bool remote_log,��ʾ��SDK��̨����log��Ϣ
			 * 	}
			 * eg:	demo������������������,logĬ�Ͼ�����.
			 * 
			 */
			Intent intent = new Intent();
			intent.setClass(this, SensoroFsmService.class);
			sensoroSense.startService(this, intent, false, null);
			
			/**
			 * ��UID.
			 * �ò���Ϊ��ѡ����,ֻ�е�APP��Ҫ���û�Ψһ�󶨵�ʱ��ʹ��.
			 * eg:	ҡһҡ���Ը��û�����һ���Ż�ȯ,���������Ż�ȯֻ����ҡ���ĸ��û�ʹ��,�����û���ѯ�����Ҳ���ʹ��.
			 * 		���,��Ҫ��service�󶨵��ض��û���,ͨ���ú������а�.
			 * 		�ú����������κ�ʱ����е���,bind��ͬ���û�.
			 */
//			sensoroSense.bindUid("4");
		} else {
			//TODO �û����д���,demo�н�ֹ����APP
			System.exit(0);
		}
	}

	/**
	 * @description ��ʼ��BLE,�ж��豸�Ƿ�֧��BLE�������Ƿ�� ��������������߱�������ж�,������ܵ���SDK��������ʹ��.
	 * @date 2014-4-21
	 * @author Sensoro
	 */
	private void initBluetooth() {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter != null) {
			if (getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_BLUETOOTH_LE)) {
				isSupportBLE = true;
			} else {
				isSupportBLE = false;
			}
			if (bluetoothAdapter.isEnabled()) {
				isBTOpen = true;
			} else {
				isBTOpen = false;
			}
		}
	}
}
