package com.sensoro.beacon.core;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


class LogUtils {

	protected static final String LINK = " - ";
	static String format= "yyyy-MM-dd hh:mm:ss";
	static SimpleDateFormat dateFormat = new SimpleDateFormat(format);
	private static final String SPACE = " ";

	/**
	 * ��ȡ��ǰ��������
	 * 
	 * @param
	 * @return
	 * @date 2014��3��28��
	 * @author trs
	 */
	public static String getCurrentMethod() {

		try {
			// ����1��ͨ��Throwable�ķ���getStackTrace()
			String name = new Throwable().getStackTrace()[1].getMethodName();
			// ����2��ͨ��Thread�ķ���getStackTrace()
			// String name = Thread.currentThread().getStackTrace()[2]
			// .getMethodName();
			return name;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * ��ȡ��ǰ�ļ�����
	 * 
	 * @param
	 * @return
	 * @date 2014��3��28��
	 * @author trs
	 */
	public static String getCurrentFile() {

		try {
			// ����1��ͨ��Throwable�ķ���getStackTrace()
			String name = new Throwable().getStackTrace()[1].getFileName();
			// ����2��ͨ��Thread�ķ���getStackTrace()
			// String name = Thread.currentThread().getStackTrace()[2]
			// .getMethodName();
			return name;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * ��ȡ��ǰ�к�
	 * 
	 * @param
	 * @return
	 * @date 2014��3��28��
	 * @author trs
	 */
	public static int getCurrentLine() {

		try {
			// ����1��ͨ��Throwable�ķ���getStackTrace()
			int no = new Throwable().getStackTrace()[1].getLineNumber();
			// ����2��ͨ��Thread�ķ���getStackTrace()
			// String name = Thread.currentThread().getStackTrace()[2]
			// .getMethodName();
			return no;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * ��ȡ��ǰ�ļ���,�кźͺ�����
	 * 
	 * @param
	 * @return
	 * @date 2014��3��28��
	 * @author trs
	 */
	public static String getCurrentLog() {

		try {
			StackTraceElement stackTraceElement = new Throwable()
					.getStackTrace()[1];

			StringBuilder builder = new StringBuilder();
			
			Date date = new Date();
			String dateString = dateFormat.format(date);
			builder.append(dateString);
			builder.append(SPACE);
			//file
			builder.append(stackTraceElement.getFileName());
			builder.append(SPACE);
			//line
			builder.append(stackTraceElement.getLineNumber());
			builder.append(SPACE);
			//method
			builder.append(stackTraceElement.getMethodName());

			return builder.toString();
		} catch (Exception e) {
			return null;
		}
	}
	public static String getCurrentLog(String info) {

		StringBuilder builder = new StringBuilder();
		
		String tmpString = getCurrentLog();
		// time, file, line, method
		builder.append(tmpString);
		builder.append(SPACE);
		//info
		if (info != null) {
			builder.append(info);
		}
		
		return builder.toString();
	}
	public static void updLog(AndroidUDP udp, String log) {
		
		if (udp == null || log == null) {
			return;
		}
		
		try {
			byte[] enBytes = UDPRC4.encodeData(log.getBytes());
			udp.send(enBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
