package com.sensoro.beacon.core;
import java.util.Random;



class UDPRC4 {
	private static final String PASSWORD_PRE = "SENS";
	//协议中是8AA8,但是实际中网络转换高低字节顺序
	private static final byte FIRST = (byte)0xA8; 	
	private static final byte SECOND = (byte)0x8A; 
	private static int[] S = new int[256];
	private static int[] T = new int[256];

	private static void init(byte[] key) {
		int keylen, j;
		int t;

		for (int jj = 0; jj < 256; jj++) {
			keylen = key.length;
			S[jj] = jj; /* put 0-255 unrepeted element into S box */
			T[jj] = key[jj % keylen];
		}

		j = 0;
		for (int jj = 0; jj < 256; jj++) {
			j = ((j + S[jj] + T[jj]) % 256) & 0xFF;
			t = S[jj];
			S[jj] = S[j];
			S[j] = t;
		}
	}
	
	public static byte[] encodeData(byte[] raw){
		byte[] salt = new byte[4];
		Random random = new Random();
		random.nextBytes(salt);
		byte[] password = byteMerger(PASSWORD_PRE.getBytes(),salt);
		init(password);
		byte[] data = encrypt(raw);
		
		byte[] result = null;
		byte[] startBytes = new byte[]{FIRST,SECOND};
		int totalLength = data.length + salt.length;
		byte[] length = shortToByte((short)totalLength);
		result = byteMerger(startBytes,length);
		result = byteMerger(result, salt);
		result = byteMerger(result,data);
		return result;
	}
	
	/**
     * 截取指定字节数组
     * @param src    源字节数组
     * @param begin    起始下标
     * @param count    截取字节个数
     * @return    新的字节数组
     */
    private static byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i=begin; i<begin+count; i++) bs[i-begin] = src[i];
        return bs;
    }
	
	public static byte[] decodeData(byte[] decodeData){
		byte[] result = null;
		if (decodeData[0] == FIRST && decodeData[1] == SECOND) {
			byte[] length = subBytes(decodeData,2,2);
			byte[] salt = subBytes(decodeData,4,4); 
			byte[] password = byteMerger(PASSWORD_PRE.getBytes(),salt);
			init(password);
			int totalLength = byteToShort(length);
			int dataLength= totalLength - 4;
			byte[] data = subBytes(decodeData,8,dataLength);
			result = decrypt(data);
		} else {
			return null;
		}
		return result;
	}
	
	/**
	 * RC4 Encryption
	 * 
	 * @param plaintext
	 * @return
	 */
	private static byte[] encrypt(byte[] plaintext) {
		int j = 0, i = 0, t, k, temp;
		int[] s;
		byte[] pt, ct;

		// deep copy
		s = S.clone();

		pt = plaintext;
		ct = new byte[pt.length];
		for (int jj = 0; jj < pt.length; jj++) {
			i = ((i + 1) % 256) & 0xFF;
			j = ((j + s[i]) % 256) & 0xFF;

			// classic swap
			temp = s[i];
			s[i] = s[j];
			s[j] = temp;

			t = ((s[i] + s[j]) % 256) & 0xFF;
			k = s[t];

			byte b = (byte) (((byte) (k & 0xFF)) ^ pt[jj]);

			// System.out.println( b );

			ct[jj] = b;
		}

		return ct;
	}

	/**
	 * Same as encryption
	 * 
	 * @param ciphertext
	 * @return
	 */
	private static byte[] decrypt(byte[] ciphertext) {
		return encrypt(ciphertext);
	}
	
	private static byte[] byteMerger(byte[] byte_1, byte[] byte_2){  
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];  
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);  
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);  
        return byte_3;  
    }
	
	 /** 
     * 注释：字节数组到short的转换！ 
     * 
     * @param b 
     * @return 
     */ 
	private static short byteToShort(byte[] b) { 
        short s = 0; 
        short s0 = (short) (b[0] & 0xff);// 最低位 
        short s1 = (short) (b[1] & 0xff); 
        s1 <<= 8; 
        s = (short) (s0 | s1); 
        return s; 
    }
	
	/** 
     * 注释：short到字节数组的转换！ 
     * 
     * @param s 
     * @return 
     */ 
    private static byte[] shortToByte(short number) { 
        int temp = number; 
        byte[] b = new byte[2]; 
        for (int i = 0; i < b.length; i++) { 
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位 
            temp = temp >> 8; // 向右移8位 
        } 
        return b; 
    } 

	public static void main(String[] args) {
		String password = "SENSOROX";
		String string = "test";
		
		byte[] send = encodeData(string.getBytes());
		byte[] raw = decodeData(send);
		System.out.println(new String(raw));
//		Random random = new Random();
//		byte[] salt = new byte[4];
//		random.nextBytes(salt);
//		byte[] pass = byteMerger(password.getBytes(), salt);
//		RC4 rc4 = new RC4(pass);
//		byte[] dt1 = rc4.encrypt(string.getBytes());
//		byte[] dt2 = rc4.encrypt(dt1);
//		System.out.println(new String(dt1));
//		System.out.println(new String(dt2));

	}
}
