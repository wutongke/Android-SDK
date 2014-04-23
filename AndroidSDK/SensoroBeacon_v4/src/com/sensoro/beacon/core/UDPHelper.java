package com.sensoro.beacon.core;

import java.io.IOException;

import android.content.Context;
import android.util.Log;

class UDPHelper {
	private static final String TAG = "UDPHelper";
	private AndroidUDP androidUDP = null;
	private int retryTimes = 0;		//重试次数
	private int retriedTimes = 0;	//已重试次数
	private int requestSpace = 750;	//重试间隔(毫秒s)
	private byte[] sendMessage = null;
	
	UDPHelper(Context context,int remotePort,String remoteIp){
		androidUDP = new AndroidUDP(context, remotePort, remoteIp, 0);
	}
	
	UDPHelper(Context context,int remotePort,String remoteIp,int timeOut,int retryTimes){
		androidUDP = new AndroidUDP(context, remotePort, remoteIp, timeOut);
		this.retryTimes = retryTimes;
	}
	
	UDPHelper(Context context,int sendPort,int listenPort,String remoteIp){
		androidUDP = new AndroidUDP(context, sendPort, listenPort, remoteIp, 0);
	}
	
	UDPHelper(Context context,int sendPort,int listenPort,String remoteIp,int timeOut,int retryTimes){
		androidUDP = new AndroidUDP(context, sendPort, listenPort, remoteIp, timeOut);
		this.retryTimes = retryTimes;
	}
	
	public byte[] swapData(byte[] message) throws TimeOutException{
		sendMessage = message;
		try {
			androidUDP.send(sendMessage);
		} catch (IOException e) {
			androidUDP.closeSocket();
			e.printStackTrace();
			Log.v(TAG, e.toString());
			return null;
		}
		byte[] result = receive();
		if (result != null) {
			return result;
		} else {
			return retrySend();
		}
	}
	
	private byte[] receive() {
		byte[] data = null;
		try {
			data = androidUDP.receive();
		} catch (IOException e) {
			androidUDP.closeSocket();
			e.printStackTrace();
		}
		return data;
	}
	
	private byte[] retrySend() throws TimeOutException{
		if (retriedTimes == retryTimes - 1) {	//重试3次(发一次,重发2次)
			retriedTimes = 0;
			throw new TimeOutException("retrid " + retryTimes +"time,time out");
		} else {
			retriedTimes++;
			Log.v(TAG, "retried times = " + retriedTimes);
			try {
				Thread.sleep(requestSpace * retriedTimes);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return swapData(sendMessage);
		}
	}

}
