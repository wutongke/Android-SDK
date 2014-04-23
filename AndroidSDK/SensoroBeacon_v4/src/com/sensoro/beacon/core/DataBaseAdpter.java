package com.sensoro.beacon.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DataBaseAdpter {

	static final String TAG = DataBaseAdpter.class.getSimpleName();
	private static final boolean DEBUG = true;

	boolean offlineWorking = false; // 存储当前离线数据存储是否在运行,如果在运行,则数据库的增删改查功能不可用
	Object lockObject = new Object(); // 正常的增删改查的数据库操作是否在运行的锁

	// 数据库存储的type和name
	static final int TYPE_SPOT = 0;
	static final int TYPE_ZONE = 1;

	static final int NAME_ENTER = 0;
	static final int NAME_LEAVE = 1;
	static final int NAME_STAY = 2;

	private static final String DB_NAME = "SENSORO_OFFLINE_DB";
	private static final int DB_VERSION = 1;
	private DatabaseHelper databaseHelper = null;
	private Context context = null;

	public SQLiteDatabase sqliteDatabase = null;
	// 表名
	public final static String TAB_BEACON_CONFIG = "beacon_config";
	public final static String TAB_ZONE_CONFIG = "zone_config";
	public final static String TAB_ACTION_RECORD = "action_record";
	// column
	public static final String _ID = "_id";
	public static final String ID = "id";
	public static final String INFO = "info";
	public static final String EQUAL = "=";
	public static final String SEPARATOR = "'";
	private static final String TYPE = "type";
	private static final String NAME = "name";
	private static final String UPDATE_TIME = "update_time";
	private static final String ASK = "?";
	private static final String TIMES = "times";
	private static final String TIMESTAMP = "timestamp";
	private static final String AND = "and";
	private static final String SPACE = " ";

	// 删除数据库表
	// beacon_config
	private static final String delTBBeaconConfig = "DROP TABLE IF EXISTS "
			+ TAB_BEACON_CONFIG;
	// zone_config
	private static final String delTBZoneConfig = "DROP TABLE IF EXISTS "
			+ TAB_ZONE_CONFIG;

	// 创建表
	// beacon_config
	private static final String createTBBeaconConfig = "Create  TABLE [beacon_config] ("
			+ "[_id] int PRIMARY KEY UNIQUE,"
			+ "[id] varchar(20) UNIQUE NOT NULL,"
			+ "[info] text(200) NOT NULL," + "[update_time] bigint" + ");";
	// zone_config
	private static final String createTBZoneConfig = "Create  TABLE [zone_config] ("
			+ "[_id] int PRIMARY KEY UNIQUE,"
			+ "[id] varchar(20) UNIQUE NOT NULL,"
			+ "[info] text(200) NOT NULL," + "[update_time] bigint" + ");";
	// action_record 该表用于记录zone或者spot触发的action
	public static final String createTBActionRecord = "Create  TABLE [action_record] ("
			+ "[_id] int PRIMARY KEY UNIQUE,"
			+ "[id] varchar(50) NOT NULL,[timestamp] bigint NOT NULL,"
			+ "[type] int NOT NULL,"
			+ "[name] int NOT NULL ,[times] int"
			+ ");";

	private static final String clearTableBeaconConfig = "delete from "
			+ TAB_BEACON_CONFIG;
	private static final String clearTableZoneConfig = "delete from "
			+ TAB_ZONE_CONFIG;
	private static final String clearTableActionRecord = "delete from "
			+ TAB_ACTION_RECORD;

	public DataBaseAdpter(Context context) {
		this.context = context;
	}

	// 打开数据库，返回数据库对象
	public void open() throws SQLException {
		databaseHelper = new DatabaseHelper(context);
		sqliteDatabase = databaseHelper.getWritableDatabase();
	}

	// 关闭数据库
	public void close() {
		databaseHelper.close();
	}

	/**
	 * 添加一条beaconConfig记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月14日
	 * @author trs
	 */
	public boolean addBeaconConfig(String id, String info, long updateTime) {

		if (offlineWorking) {
			loggerInfo("addBeaconConfig sqliteDatabase is lock");
			return false;
		}
		synchronized (lockObject) {
			sqliteDatabase.beginTransaction();

			ContentValues contentValues = new ContentValues();
			contentValues.put(ID, id);
			contentValues.put(INFO, info);
			contentValues.put(UPDATE_TIME, updateTime);

			long error = 0;
			try {
				error = sqliteDatabase.insertOrThrow(TAB_BEACON_CONFIG, _ID,
						contentValues);
			} catch (SQLException e) {
				sqliteDatabase.endTransaction();
				return false;
			}

			if (error == -1) {
				sqliteDatabase.endTransaction();
				return false;
			}

			sqliteDatabase.setTransactionSuccessful();
			sqliteDatabase.endTransaction();
			return true;
		}

	}

	/**
	 * 添加一条beaconConfig记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月14日
	 * @author trs
	 */
	public boolean addBeaconConfigNoLock(String id, String info, long updateTime) {

		synchronized (lockObject) {
			sqliteDatabase.beginTransaction();

			ContentValues contentValues = new ContentValues();
			contentValues.put(ID, id);
			contentValues.put(INFO, info);
			contentValues.put(UPDATE_TIME, updateTime);

			long error = 0;
			try {
				error = sqliteDatabase.insertOrThrow(TAB_BEACON_CONFIG, _ID,
						contentValues);
			} catch (SQLException e) {
				sqliteDatabase.endTransaction();
				return false;
			}

			if (error == -1) {
				sqliteDatabase.endTransaction();
				return false;
			}

			sqliteDatabase.setTransactionSuccessful();
			sqliteDatabase.endTransaction();
			return true;
		}

	}

	/**
	 * 更新一条beaconConfig记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月17日
	 * @author zwz
	 */
	public int updateBeaconConfig(String id, String info, long updateTime) {

		if (sqliteDatabase == null) {
			return -1;
		}
		if (offlineWorking) {
			loggerInfo("updateBeaconConfig sqliteDatabase is lock");
			return -1;
		}
		synchronized (lockObject) {
			StringBuilder wherebuilder = new StringBuilder();

			wherebuilder.append(ID);
			wherebuilder.append(EQUAL);
			wherebuilder.append(ASK);

			ContentValues contentValues = new ContentValues();
			contentValues.put(INFO, info);
			contentValues.put(UPDATE_TIME, updateTime);

			int res = sqliteDatabase.update(TAB_BEACON_CONFIG, contentValues,
					wherebuilder.toString(), new String[] { id });
			return res;
		}

	}

	/**
	 * 更新一条beaconConfig记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月17日
	 * @author trs
	 */
	public int updateBeaconConfigNoLock(String id, String info, long updateTime) {

		if (sqliteDatabase == null) {
			return -1;
		}
		synchronized (lockObject) {
			StringBuilder wherebuilder = new StringBuilder();

			wherebuilder.append(ID);
			wherebuilder.append(EQUAL);
			wherebuilder.append(ASK);

			ContentValues contentValues = new ContentValues();
			contentValues.put(INFO, info);
			contentValues.put(UPDATE_TIME, updateTime);

			int res = sqliteDatabase.update(TAB_BEACON_CONFIG, contentValues,
					wherebuilder.toString(), new String[] { id });
			return res;
		}

	}

	/**
	 * 查询一条beaconConfig记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月14日
	 * @author trs
	 */
	public String getBeaconConfigInfo(String id) {

		if (offlineWorking) {
			loggerInfo("getBeaconConfigInfo sqliteDatabase is lock");
			return null;
		}
		synchronized (lockObject) {
			String info = null;
			StringBuilder builder = new StringBuilder();
			builder.append(ID);
			builder.append(EQUAL);
			builder.append(SEPARATOR);
			builder.append(id);
			builder.append(SEPARATOR);
			try {
				Cursor cursor = sqliteDatabase.query(TAB_BEACON_CONFIG, null,
						builder.toString(), null, null, null, null);
				boolean res = cursor.moveToFirst();
				// Log.i(TAG, cursor.getCount() + "");
				if (!res) {
					// 无数据
					return null;
				}
				info = cursor.getString(cursor.getColumnIndex(INFO));

			} catch (SQLException e) {
				return null;
			}

			return info;
		}

	}

	/**
	 * 查询一条beaconConfig记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月14日
	 * @author trs
	 */
	public BeaconConfigEx getBeaconConfigInfoNoLock(String id) {

		synchronized (lockObject) {
			BeaconConfigEx beaconConfigEx = null;
			StringBuilder builder = new StringBuilder();
			builder.append(ID);
			builder.append(EQUAL);
			builder.append(SEPARATOR);
			builder.append(id);
			builder.append(SEPARATOR);
			try {
				Cursor cursor = sqliteDatabase.query(TAB_BEACON_CONFIG, null,
						builder.toString(), null, null, null, null);
				boolean res = cursor.moveToFirst();
				// Log.i(TAG, cursor.getCount() + "");
				if (!res) {
					// 无数据
					return null;
				}
				beaconConfigEx = new BeaconConfigEx();

				beaconConfigEx.beaconConfigJson = cursor.getString(cursor
						.getColumnIndex(INFO));
				beaconConfigEx._date = cursor.getLong(cursor
						.getColumnIndex(UPDATE_TIME));

			} catch (SQLException e) {
				return null;
			}

			return beaconConfigEx;
		}

	}

	/**
	 * 清空BeaconConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年4月15日
	 * @author trs
	 */
	public void clearBeaconConfig() {

		if (offlineWorking) {
			loggerInfo("clearBeaconConfig sqliteDatabase is lock");
			return;
		}
		synchronized (lockObject) {
			sqliteDatabase.beginTransaction();

			int count = sqliteDatabase.delete(clearTableBeaconConfig, null,
					null);
			Log.i(TAG, "remove ZoneConfig count - " + count);
		}

	}

	/**
	 * 添加一条ZoneConfig记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月14日
	 * @author trs
	 */
	public boolean addZoneConfig(String id, String info, long updateTime) {

		if (offlineWorking) {
			loggerInfo("addZoneConfig sqliteDatabase is lock");
			return false;
		}
		synchronized (lockObject) {
			sqliteDatabase.beginTransaction();

			ContentValues contentValues = new ContentValues();
			contentValues.put(ID, id);
			contentValues.put(INFO, info);
			contentValues.put(UPDATE_TIME, updateTime);
			long error = 0;
			try {
				error = sqliteDatabase.insertOrThrow(TAB_ZONE_CONFIG, _ID,
						contentValues);
			} catch (SQLException e) {
				sqliteDatabase.endTransaction();
				return false;
			}

			if (error == -1) {
				sqliteDatabase.endTransaction();
				return false;
			}

			sqliteDatabase.setTransactionSuccessful();
			sqliteDatabase.endTransaction();
			return true;
		}

	}

	/**
	 * 添加一条ZoneConfig记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月14日
	 * @author trs
	 */
	public boolean addZoneConfigNoLock(String id, String info, long updateTime) {

		synchronized (lockObject) {
			sqliteDatabase.beginTransaction();

			ContentValues contentValues = new ContentValues();
			contentValues.put(ID, id);
			contentValues.put(INFO, info);
			contentValues.put(UPDATE_TIME, updateTime);
			long error = 0;
			try {
				error = sqliteDatabase.insertOrThrow(TAB_ZONE_CONFIG, _ID,
						contentValues);
			} catch (SQLException e) {
				sqliteDatabase.endTransaction();
				return false;
			}

			if (error == -1) {
				sqliteDatabase.endTransaction();
				return false;
			}

			sqliteDatabase.setTransactionSuccessful();
			sqliteDatabase.endTransaction();
			return true;
		}

	}

	/**
	 * 添加一条zoneConfig记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月14日
	 * @author trs
	 */
	public String getZoneConfigInfo(String id) {

		if (offlineWorking) {
			loggerInfo("getZoneConfigInfo sqliteDatabase is lock");
			return null;
		}

		synchronized (lockObject) {
			String info = null;
			StringBuilder builder = new StringBuilder();
			builder.append(ID);
			builder.append(EQUAL);
			builder.append(SEPARATOR);
			builder.append(id);
			builder.append(SEPARATOR);
			try {
				Cursor cursor = sqliteDatabase.query(TAB_ZONE_CONFIG, null,
						builder.toString(), null, null, null, null);
				boolean res = cursor.moveToFirst();
				// Log.i(TAG, cursor.getCount() + "");
				if (!res) {
					// 无数据
					return null;
				}
				info = cursor.getString(cursor.getColumnIndex(INFO));

			} catch (SQLException e) {
				return null;
			}

			return info;
		}

	}

	/**
	 * 获取ZoneConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年4月21日
	 * @author trs
	 */
	public ZoneConfigEx getZoneConfigInfoNoLock(String id) {

		synchronized (lockObject) {
			ZoneConfigEx zoneConfigEx = null;
			StringBuilder builder = new StringBuilder();

			builder.append(ID);
			builder.append(EQUAL);
			builder.append(SEPARATOR);
			builder.append(id);
			builder.append(SEPARATOR);
			try {
				Cursor cursor = sqliteDatabase.query(TAB_ZONE_CONFIG, null,
						builder.toString(), null, null, null, null);
				boolean res = cursor.moveToFirst();
				// Log.i(TAG, cursor.getCount() + "");
				if (!res) {
					// 无数据
					return null;
				}
				zoneConfigEx = new ZoneConfigEx();
				zoneConfigEx.zoneConfigJson = cursor.getString(cursor
						.getColumnIndex(INFO));
				zoneConfigEx._date = cursor.getLong(cursor
						.getColumnIndex(UPDATE_TIME));
			} catch (SQLException e) {
				return null;
			}

			return zoneConfigEx;
		}

	}

	/**
	 * 更新ZoneConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年4月21日
	 * @author trs
	 */
	public int updateZoneConfig(String id, String info, long updateTime) {

		if (sqliteDatabase == null) {
			return -1;
		}
		if (offlineWorking) {
			loggerInfo("updateZoneConfig sqliteDatabase is lock");
			return -1;
		}
		synchronized (lockObject) {
			StringBuilder wherebuilder = new StringBuilder();

			wherebuilder.append(ID);
			wherebuilder.append(EQUAL);
			wherebuilder.append(ASK);

			ContentValues contentValues = new ContentValues();
			contentValues.put(INFO, info);
			contentValues.put(UPDATE_TIME, updateTime);

			int res = sqliteDatabase.update(TAB_ZONE_CONFIG, contentValues,
					wherebuilder.toString(), new String[] { id });
			return res;
		}

	}

	/**
	 * 更新ZoneConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年4月21日
	 * @author trs
	 */
	public int updateZoneConfigNoLock(String id, String info, long updateTime) {

		if (sqliteDatabase == null) {
			return -1;
		}
		synchronized (lockObject) {
			StringBuilder wherebuilder = new StringBuilder();

			wherebuilder.append(ID);
			wherebuilder.append(EQUAL);
			wherebuilder.append(ASK);

			ContentValues contentValues = new ContentValues();
			contentValues.put(INFO, info);
			contentValues.put(UPDATE_TIME, updateTime);

			int res = sqliteDatabase.update(TAB_ZONE_CONFIG, contentValues,
					wherebuilder.toString(), new String[] { id });
			return res;
		}

	}

	/**
	 * 清空ZoneConfig
	 * 
	 * @param
	 * @return
	 * @date 2014年4月15日
	 * @author trs
	 */
	public void clearZoneConfig() {

		if (offlineWorking) {
			loggerInfo("clearZoneConfig sqliteDatabase is lock");
			return;
		}
		synchronized (lockObject) {

			sqliteDatabase.beginTransaction();
			int count = sqliteDatabase.delete(clearTableZoneConfig, null, null);
			Log.i(TAG, "remove ZoneConfig count - " + count);
			sqliteDatabase.setTransactionSuccessful();
			sqliteDatabase.endTransaction();
		}

	}

	public boolean addActionRecord(String id, int type, int name,
			long timestamp, int times) {

		if (offlineWorking) {
			loggerInfo("addActionRecord sqliteDatabase is lock");
			return false;
		}
		synchronized (lockObject) {
			if (sqliteDatabase == null) {
				return false;
			}

			ContentValues values = new ContentValues();

			values.put(ID, id);
			values.put(TYPE, type);
			values.put(NAME, name);
			values.put(TIMESTAMP, timestamp);
			values.put(TIMES, times);

			sqliteDatabase.beginTransaction();
			long res = sqliteDatabase.insertOrThrow(TAB_ACTION_RECORD, _ID, values);
			if (res == -1) {
				sqliteDatabase.endTransaction();
				return false;
			}
			sqliteDatabase.setTransactionSuccessful();
			sqliteDatabase.endTransaction();
		}

		return true;
	}

	/**
	 * 查询action_record记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月16日
	 * @author trs
	 */
	public ActionRecord getActionRecord(String id, int type, int name) {

		if (sqliteDatabase == null) {
			return null;
		}
		if (offlineWorking) {
			loggerInfo("getActionRecord sqliteDatabase is lock");
			return null;
		}
		synchronized (lockObject) {
			StringBuilder wherebuilder = new StringBuilder();

			wherebuilder.append(ID);
			wherebuilder.append(EQUAL);
			wherebuilder.append(ASK);

			wherebuilder.append(SPACE);
			wherebuilder.append(AND);
			wherebuilder.append(SPACE);

			wherebuilder.append(TYPE);
			wherebuilder.append(EQUAL);
			wherebuilder.append(ASK);

			wherebuilder.append(SPACE);
			wherebuilder.append(AND);
			wherebuilder.append(SPACE);

			wherebuilder.append(NAME);
			wherebuilder.append(EQUAL);
			wherebuilder.append(ASK);

			Cursor cursor = sqliteDatabase.query(TAB_ACTION_RECORD, null,
					wherebuilder.toString(), new String[] { id, type + "",
							name + "" }, null, null, null);
			boolean res = cursor.moveToFirst();
			if (!res) {
				return null;
			}

			ActionRecord record = new ActionRecord();
			record._id = cursor.getInt(cursor.getColumnIndex(_ID));
			record.id = cursor.getString(cursor.getColumnIndex(ID));
			record.type = cursor.getInt(cursor.getColumnIndex(TYPE));
			record.name = cursor.getInt(cursor.getColumnIndex(NAME));
			record.timestamp = cursor.getLong(cursor.getColumnIndex(TIMESTAMP));
			record.times = cursor.getInt(cursor.getColumnIndex(TIMES));

			return record;
		}

	}

	/**
	 * 更新action_record记录,如果数据库中没有则添加记录
	 * 
	 * @param
	 * @return
	 * @date 2014年4月16日
	 * @author trs
	 */
	public int updateActionRecord(String id, int type, int name,
			long timestamp, int times) {

		if (sqliteDatabase == null) {
			return -1;
		}
		if (offlineWorking) {
			loggerInfo("updateActionRecord sqliteDatabase is lock");
			return -1;
		}

		synchronized (lockObject) {
			StringBuilder wherebuilder = new StringBuilder();

			wherebuilder.append(ID);
			wherebuilder.append(EQUAL);
			wherebuilder.append(ASK);

			wherebuilder.append(SPACE);
			wherebuilder.append(AND);
			wherebuilder.append(SPACE);

			wherebuilder.append(TYPE);
			wherebuilder.append(EQUAL);
			wherebuilder.append(ASK);

			wherebuilder.append(SPACE);
			wherebuilder.append(AND);
			wherebuilder.append(SPACE);

			wherebuilder.append(NAME);
			wherebuilder.append(EQUAL);
			wherebuilder.append(ASK);

			ContentValues values = new ContentValues();
			values.put(TIMESTAMP, timestamp);
			values.put(TIMES, times);

			int res = sqliteDatabase.update(TAB_ACTION_RECORD, values,
					wherebuilder.toString(), new String[] { id, type + "",
							name + "" });

			return res;
		}

	}

	/**
	 * 设置离线数据库存储标志,该标志如果为true,则说明正在进行离线数据的存储
	 * 
	 * @param
	 * @return
	 * @date 2014年4月21日
	 * @author trs
	 */
	public void lockOfflineWorking() {
		offlineWorking = true;
	}

	/**
	 * 设置离线数据库存储标志
	 * 
	 * @param
	 * @return
	 * @date 2014年4月21日
	 * @author trs
	 */
	public void unlockOfflineWorking() {
		offlineWorking = false;
	}

	private class DatabaseHelper extends SQLiteOpenHelper {
		/* 构造函数-创建一个数据库 */
		DatabaseHelper(Context context) {
			// 当调用getWritableDatabase()
			// 或 getReadableDatabase()方法时
			// 则创建一个数据库
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// 数据库建表
			try {
				synchronized (lockObject) {
					db.execSQL(createTBBeaconConfig);
					db.execSQL(createTBZoneConfig);
					db.execSQL(createTBActionRecord);
				}

			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}

		}

		/* 升级数据库 */
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			try {
				// db.execSQL(m_DelTB_BeaconConfig);
				// db.execSQL(m_DelTB_ZoneConfig);
				// onCreate(db);
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	private void loggerInfo(String log) {
		if (DEBUG) {
			Log.i(TAG, log);
		}
	}
}
