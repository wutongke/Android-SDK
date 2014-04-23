package com.sensoro.beacon.core;

import com.sensoro.beacon.base.WifiBeacon;

class WIFIBeaconUtils {
	private final static char[] hexArray = "0123456789abcdef".toCharArray();
	private static String PREFIX = "e2938820";
	private static final String regularExpression = "([0-9a-f]{8})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{12})";
	private static final String replacement = "$1-$2-$3-$4-$5";
	private static String KEY = "caffebee";
	private static int PREFIX_LEN = PREFIX.length();
	private static int SSID_LENGTH = 60;
	
	public static WifiBeacon GetWifiBeaconBySSID(String ssid) {
		
		if (ssid.length() != SSID_LENGTH) {
			return null;
		}
		
		int length = ssid.length();
		String ssid_prefix = ssid.substring(0, PREFIX_LEN);
		if (!ssid_prefix.equals(PREFIX)) {
			//如果头不相等,说明不是wifi beacon
			return null;
		}
		//获取后缀,用于解密,2个字符
		String suffix = ssid.substring(ssid.length()-2);
		//根据后两位生成解密key
		String key = String.format("%s%s", suffix, KEY); 
		WifiRC4 rc4 = new WifiRC4(hexToByte(key));
		String encrypt = ssid.substring(PREFIX_LEN, length-2);
		//Base91解码
		byte[] base91Bytes = Base91.decode(hexToByte(encrypt));
		//rc4 解密
		byte[] rc4Bytes = rc4.decrypt(base91Bytes);
		//原始的编码(字符串)
		int len = rc4Bytes.length;
		String uuid = bytesToHex(rc4Bytes, len-4);
		uuid = uuid.replaceAll(regularExpression, replacement).toUpperCase();
		int major = ((rc4Bytes[len-4] & 0xff) << 8) + (rc4Bytes[len-3] & 0xff);
        int minor = ((rc4Bytes[len-2] & 0xff) << 8) + (rc4Bytes[len-1] & 0xff);
        WifiBeacon wifiBeacon = new WifiBeacon(uuid, major, minor);
		return wifiBeacon;
	}
	
	private static String bytesToHex(byte[] bytes, int length) {
		int len = 0;
		if (length >= bytes.length) {
			len = bytes.length;
		} else {
			len = length;
		}
        char[] hexChars = new char[len * 2];
        for ( int j = 0; j < len; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
	
	private static byte[] hexToByte(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
