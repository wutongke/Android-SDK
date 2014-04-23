package com.sensoro.beacon.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.content.Context;
import android.net.wifi.WifiManager;

class AndroidUDP {
	public static final int SOCKET_SIAGLE = 1;
	public static final int SOCKET_MULTIPLY = 2;
	
	private WifiManager.MulticastLock multicastLock = null;
	private WifiManager wifiManager = null;
	
	private String remoteIp = null;
	private int timeOut = 0;
	private int socketMode = 0;
	
	private int remotePort = 0;
	private DatagramSocket socketSingle = null;
	
	private int sendPort = 0;
	private int listenPort = 0;
	private DatagramSocket socketSend = null;
	private DatagramSocket socketReceive = null;
	
	public AndroidUDP(Context context, int remotePort, String remoteIp,
			int timeOut) {
		wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifiManager.createMulticastLock("AndroidUDP");
		this.remoteIp = remoteIp;
		this.timeOut = timeOut;
		this.remotePort = remotePort;
		socketMode = SOCKET_SIAGLE;
	}

	public AndroidUDP(Context context,int sendPort,int listenPort,String remoteIp,int timeOut){
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifiManager.createMulticastLock("AndroidUDP");
		this.remoteIp = remoteIp;
		this.timeOut = timeOut;
		this.sendPort = sendPort;
		this.listenPort = listenPort;
		socketMode = SOCKET_MULTIPLY;
	}
		public AndroidUDP(Context context, int remotePort, String remoteIp,
			int timeOut, int mode) {
		wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifiManager.createMulticastLock("AndroidUDP");
		this.remoteIp = remoteIp;
		this.timeOut = timeOut;
		this.remotePort = remotePort;
		if (mode != SOCKET_SIAGLE && mode != SOCKET_MULTIPLY) {
			socketMode = SOCKET_SIAGLE;
		} else {
			socketMode = mode;
		}

	}
	public void send(byte[] message) throws IOException {
		message = RC4.encodeData(message);
		InetAddress host = null;
		DatagramPacket packet = null;
		host = InetAddress.getByName(remoteIp);
		if (socketMode == SOCKET_SIAGLE) {
			socketSingle = new DatagramSocket(remotePort);
			socketSingle.setSoTimeout(timeOut);
			packet = new DatagramPacket(message, message.length, host,
					remotePort);
			socketSingle.send(packet);
		} else if (socketMode == SOCKET_MULTIPLY) {
			socketSend = new DatagramSocket(remotePort);
			packet = new DatagramPacket(message, message.length, host, remotePort);
			socketSend.send(packet);
			socketSend.close();
		}
    }
	
	public byte[] receive() throws IOException{
		byte data [] = new byte[10240];
		DatagramPacket packet = new DatagramPacket(data,data.length);
		multicastLock.acquire();
		if (socketMode == SOCKET_SIAGLE) {
			socketSingle.receive(packet);
			socketSingle.close();
		} else {
			socketSingle = new DatagramSocket(listenPort);
			socketReceive.setSoTimeout(timeOut);
			socketReceive.receive(packet);
			socketReceive.close();
		}
		multicastLock.release();
		byte[] result = packet.getData();
		result = RC4.decodeData(result);
		return result;
	}
	
	public void closeSocket(){
		if (socketSingle != null) {
			socketSingle.close();
		}
		if (socketSend != null) {
			socketSend.close();
		}
		if (socketReceive != null) {
			socketSend.close();
		}
	}
}
