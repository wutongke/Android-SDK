package com.sensoro.sdkdemo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.secsoro.sdkdemo.R;

public class SensoroSDKDemoActivity extends Activity implements OnClickListener {
	public static final String SENSORO_ACTION = "android.intent.action.SENSORO_ACTION"; // �Զ���㲥action����
	public static final String STRING_LINE_FEED = "\n";
	public static final String STRING_ENTER_SPOT = "enter_spot:";
	public static final String STRING_ATION = "ation:";

	public static final String MAJOR = "  major=";
	public static final String MINOR = "  minor=";
	public static final String ZONE = "  zone=";
	public static final String SPOT = "  spot=";
	public static final String SECONDS = "  seconds=";

	public static final String ACTIVE_EVENT = "fixedcorner";
	public static final String TYPE = "type";
	/** sdk�����Ƿ��ص���Ϣ����ӡ������ */
	private EditText outputEt;
	/** ��մ�ӡ��Ϣ,��Ϣ��ʱ��̫���˾������ */
	private Button clearBtn;
	/** ��ӡ��Ϣ���ַ���,��sdk�ص�����ʱ,���ص�����Ϣ��ӽ�ȥ,��ӡ��EditText�� */
	private StringBuilder logStr = new StringBuilder();

	private SensorManager mSensorManager = null;
	private boolean isInFixCorner = false; // �Ƿ�����Խ��

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sdk_demo);
		init();
	}

	/** ��ʼ�����漰Receiver�ȵ� */
	private void init() {
		outputEt = (EditText) findViewById(R.id.output_et);
		outputEt.setKeyListener(null);
		outputEt.setMovementMethod(ScrollingMovementMethod.getInstance());
		clearBtn = (Button) findViewById(R.id.clear_edittext_button);
		clearBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				outputEt.setText("");
				logStr = new StringBuilder();
			}
		});
		logStr.append(outputEt.getText().toString());
		registerMyReceiver();// ע��MyReceiver
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	}

	/** ע��MyReceiver */
	private void registerMyReceiver() {
		IntentFilter filter = new IntentFilter(SensoroFsmService.SENSORO_ACTION);
		registerReceiver(sensoroBroadcastReceiver, filter);// ע��MyReceiver
	}

	@Override
	public void onClick(View v) {
	}

	private BroadcastReceiver sensoroBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String stringExtra = intent
					.getStringExtra(SensoroFsmService.BROADCAST_NAME);
			if (stringExtra.equals(SensoroFsmService.ENTER_FIXCORNER)) {
				isInFixCorner = true;
				logStr.append(context.getString(R.string.enter_fixedcorner)
						+ STRING_LINE_FEED);
			} else if (stringExtra.equals(SensoroFsmService.LEAVE_FIXCORNER)) {
				isInFixCorner = false;
				logStr.append(context.getString(R.string.leave_fixedcorner)
						+ STRING_LINE_FEED);
			} else if (stringExtra.equals(SensoroFsmService.ENTER_MESSAGE)) {
				String message = intent
						.getStringExtra(SensoroFsmService.ENTER_MESSAGE);
				logStr.append(message + STRING_LINE_FEED);
			}

			outputEt.setText(logStr);
			outputEt.setSelection(outputEt.getText().length(), outputEt
					.getText().length());
		}
	};

	/**
	 * ������Ӧ����
	 */
	private SensorEventListener mSensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			// ��������Ϣ�ı�ʱִ�и÷���
			float[] values = event.values;
			float x = values[0]; // x�᷽����������ٶȣ�����Ϊ��
			float y = values[1]; // y�᷽����������ٶȣ���ǰΪ��
			float z = values[2]; // z�᷽����������ٶȣ�����Ϊ��
			// Log.i(TAG, "x�᷽����������ٶ�" + x + "��y�᷽����������ٶ�" + y + "��z�᷽����������ٶ�" +
			// z);
			// һ����������������������ٶȴﵽ40�ʹﵽ��ҡ���ֻ���״̬��
			int medumValue = 14;// ���� i9250��ô�ζ����ᳬ��20��û�취��ֻ����19��,С�׸���ĳ�14
			if (Math.abs(x) > medumValue || Math.abs(y) > medumValue
					|| Math.abs(z) > medumValue) {
				// ����ﵽҡһҡ�ı�׼,�ж��Ƿ����ڽ��л�ȡ�Ż�ȯ����
				if (isInFixCorner) {
					logStr.append(getString(R.string.shark_it_off)
							+ STRING_LINE_FEED);
					outputEt.setText(logStr);
					outputEt.setSelection(outputEt.getText().length(), outputEt
							.getText().length());
					// TODO ҡһҡ���ȡ���Spot�������Զ����Params����
					// �����������һЩ����,�����Url,���߿���Activity
				}
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	@Override
	protected void onResume() {
		if (mSensorManager != null) {// ע�������
			mSensorManager.registerListener(mSensorEventListener,
					mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
					SensorManager.SENSOR_DELAY_NORMAL);
			// ��һ��������Listener���ڶ������������ô��������ͣ�����������ֵ��ȡ��������Ϣ��Ƶ��
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (mSensorManager != null) {// ȡ��������
			mSensorManager.unregisterListener(mSensorEventListener);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(sensoroBroadcastReceiver);// ȡ��
	}
}
