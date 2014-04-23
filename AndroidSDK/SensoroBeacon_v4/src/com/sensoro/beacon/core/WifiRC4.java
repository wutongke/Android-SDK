package com.sensoro.beacon.core;

/**
 * 
 * This is a class that Implements the RC4 encryption algorithm.
 * 
 * 
 * @author EJJ
 * @date Feb 16, 2012
 */

class WifiRC4 {

	private byte[] S = new byte[256];
	private byte[] T = new byte[256];

	public WifiRC4(byte[] key) {
		int keylen, j;
		byte t;

		for (int jj = 0; jj < 256; jj++) {
			keylen = key.length;
			S[jj] = (byte) jj;
			T[jj] = (byte) key[jj % keylen];
		}

		j = 0;
		for (int jj = 0; jj < 256; jj++) {
			j = ((j + S[jj] + T[jj]) % 256) & 0xFF;

			t = S[jj];
			S[jj] = S[j];
			S[j] = t;
		}
	}

	/**
	 * RC4 Encryption
	 * 
	 * @param plaintext
	 * @return
	 */
	public byte[] encrypt(byte[] plaintext) {
		int j = 0, i = 0, t, k;
		byte temp;
		byte[] pt, ct, s;

		// deep copy
		s = S.clone();

		pt = plaintext;
		ct = new byte[pt.length];
		for (int jj = 0; jj < pt.length; jj++) {
			i = ((i + 1) % 256) & 0xFF;
			j = ((j + s[i]) % 256) & 0xFF;

			// classic swap
			temp = s[jj];
			s[jj] = s[j];
			s[j] = temp;

			t = ((s[i] + s[j]) % 256) & 0xFF;

			k = s[t];

			ct[jj] = (byte) (k ^ pt[jj]);

		}

		return ct;
	}

	/**
	 * Same as encryption
	 * 
	 * @param ciphertext
	 * @return
	 */
	public byte[] decrypt(byte[] ciphertext) {
		return encrypt(ciphertext);
	}
}
