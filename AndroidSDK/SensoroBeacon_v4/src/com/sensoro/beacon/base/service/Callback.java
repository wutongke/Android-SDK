package com.sensoro.beacon.base.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

public class Callback {
	private String TAG = "Callback";
	private Intent intent;
	public Callback(String intentPackageName) {
		if (intentPackageName != null) {
			intent = new Intent();
            intent.setComponent(new ComponentName(intentPackageName, "com.sensoro.beacon.base.IBeaconIntentProcessor"));
        }
	}
	public Intent getIntent() {
		return intent;
	}
	public void setIntent(Intent intent) {
		this.intent = intent;
	}
	/**
	 * Tries making the callback, first via messenger, then via intent
	 * 
	 * @param context
	 * @param dataName
	 * @param data
	 * @return false if it callback cannot be made
	 */
	public boolean call(Context context, String dataName, Parcelable data) {
		if (intent != null) {
			Log.d(TAG, "attempting callback via intent: "+intent.getComponent());
			intent.putExtra(dataName, data);
			context.startService(intent);		
			return true;			
		}
		return false;
	}
}
