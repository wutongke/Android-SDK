package com.sensoro.beacon.core;

import java.io.File;

import org.apache.log4j.Level;

import android.os.Environment;
import android.util.Log;
import de.mindpipe.android.logging.log4j.LogConfigurator;

class AndroidLog4J {
	private static final String TAG = "AndroidLog4J";

	public static void InitLog4j() {
		try {
			LogConfigurator logConfigurator = new LogConfigurator();
			logConfigurator.setRootLevel(Level.DEBUG);
			logConfigurator.setLevel("org.apache", Level.ERROR);
			logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
			logConfigurator.setMaxFileSize(1024 * 1024 * 5);
			logConfigurator.setImmediateFlush(true);
			logConfigurator.setFileName(Environment
					.getExternalStorageDirectory()
					+ File.separator
					+ "SensoroSDK"
					+ File.separator + "logs" + File.separator + "log4j.txt");
			logConfigurator.configure();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}
}
