package com.sensoro.beacon.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

class HttpUtils {
	private Context context;
	static final String HTTP_GET = "GET";
	static final String HTTP_POST = "POST";
	static final String HTTP_DEL = "DEL";
	static final String HTTP_PUT = "PUT";
	Logger logger = Logger.getLogger(HttpUtils.class);

	public HttpUtils(Context context) {
		super();
		this.context = context;
	}

	private static final String TAG = "HttpUtils";

	/**
	 * ��������ͨ��
	 * 
	 * @param
	 * @return
	 * @date 2014��2��10��
	 * @author trs
	 */
	public String NetCommunicate(String urlString,
			HashMap<String, String> headerMap, HttpEntity body,
			HttpParams httpParams, String method) {
		String result = null;

		try {
			// ֤����֤
			// ��ȡHttpClient����
			HttpClient httpClient = new DefaultHttpClient();
			KeyStore keyStore = GetCer();
			SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore);
			Scheme sch = new Scheme("https", socketFactory, 443);
			httpClient.getConnectionManager().getSchemeRegistry().register(sch);

			if (method.toUpperCase().equals(HTTP_GET)) {
				// �½�HttpGet����
				HttpGet httpGet = new HttpGet(urlString);
				if (httpParams != null) {
					httpGet.setParams(httpParams);
				}
				if (headerMap != null) {
					Set<String> set = headerMap.keySet();
					String value = null;
					for (String key : set) {
						value = headerMap.get(key);
						httpGet.addHeader(key, value);
					}
				}
				HttpResponse httpResp = httpClient.execute(httpGet);
				// �ж��ǹ�����ɹ�
				if (httpResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// ��ȡ���ص�����
					result = EntityUtils
							.toString(httpResp.getEntity(), "UTF-8");
					Log.i(TAG, "HttpGet��ʽ����ɹ��������������£�");
					Log.i(TAG, result);
				} else {
					Log.i(TAG, "HttpGet��ʽ����ʧ��");
					logger.error("[HttpUtils NetCommunicate] " + urlString);
					logger.error("[HttpUtils NetCommunicate] HttpGet��ʽ����ʧ��");
				}
			} else if (method.toUpperCase().equals(HTTP_POST)) {
				// �½�HttpPost����
				HttpPost httpPost = new HttpPost(urlString);
				// post parameters
				if (httpParams != null) {
					httpPost.setParams(httpParams);
				}
				if (headerMap != null) {
					Set<String> set = headerMap.keySet();
					String value = null;
					for (String key : set) {
						value = headerMap.get(key);
						httpPost.addHeader(key, value);
					}
				}
				// Post entity
				if (body != null) {
					// ���ò���ʵ��
					httpPost.setEntity(body);
				}
				// ��ȡHttpResponseʵ��
				HttpResponse httpResp = httpClient.execute(httpPost);
				// �ж��ǹ�����ɹ�
				if (httpResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// ��ȡ���ص�����
					result = EntityUtils
							.toString(httpResp.getEntity(), "UTF-8");
					Log.i(TAG, "HttpPost��ʽ����ɹ��������������£�");
					Log.i(TAG, result);
				} else {
					Log.i(TAG, "HttpPost��ʽ����ʧ��");
					logger.error("[HttpUtils NetCommunicate] " + urlString);
					logger.error("[HttpUtils NetCommunicate] HttpPost��ʽ����ʧ��");
				}
			} else if (method.toUpperCase().equals(HTTP_PUT)) {
				// �½�HttpPut����
				HttpPut httpPut = new HttpPut(urlString);
				// put parameters
				if (httpParams != null) {
					httpPut.setParams(httpParams);
				}
				if (headerMap != null) {
					Set<String> set = headerMap.keySet();
					String value = null;
					for (String key : set) {
						value = headerMap.get(key);
						httpPut.addHeader(key, value);
					}
				}
				// Put entity
				if (body != null) {
					// ���ò���ʵ��
					httpPut.setEntity(body);
				}
				// ��ȡHttpResponseʵ��
				HttpResponse httpResp = httpClient.execute(httpPut);
				// �ж��ǹ�����ɹ�
				if (httpResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// ��ȡ���ص�����
					result = EntityUtils
							.toString(httpResp.getEntity(), "UTF-8");
					Log.i(TAG, "HttpPut��ʽ����ɹ��������������£�");
					Log.i(TAG, result);
				} else {
					Log.i(TAG, "HttpPut��ʽ����ʧ��");
					logger.error("[HttpUtils NetCommunicate] " + urlString);
					logger.error("[HttpUtils NetCommunicate] HttpPut��ʽ����ʧ��");
				}
			} else if (method.toUpperCase().equals(HTTP_DEL)) {
				// �½�HttpDelete����
				HttpDelete httpDelete = new HttpDelete(urlString);
				if (httpParams != null) {
					httpDelete.setParams(httpParams);
				}
				if (headerMap != null) {
					Set<String> set = headerMap.keySet();
					String value = null;
					for (String key : set) {
						value = headerMap.get(key);
						httpDelete.addHeader(key, value);
					}
				}
				// ��ȡHttpResponseʵ��
				HttpResponse httpResp = httpClient.execute(httpDelete);
				// �ж��ǹ�����ɹ�
				if (httpResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// ��ȡ���ص�����
					result = EntityUtils
							.toString(httpResp.getEntity(), "UTF-8");
					Log.i(TAG, "httpDelete��ʽ����ɹ��������������£�");
					Log.i(TAG, result);
				} else {
					Log.i(TAG, "httpDelete��ʽ����ʧ��");
					logger.error("[HttpUtils NetCommunicate] " + urlString);
					logger.error("[HttpUtils NetCommunicate] HttpDelete��ʽ����ʧ��");
				}
			} 
		} catch (Exception e) {
			Log.i(TAG, e.toString());
			logger.error("[HttpUtils NetCommunicate] " + e.toString());
		}

		return result;
	}

	/**
	 * ��������ͨ��
	 * 
	 * @param
	 * @return
	 * @date 2014��2��10��
	 * @author trs
	 */
	public InputStream NetCommunicateStream(String urlString,
			HashMap<String, String> headerMap, HttpEntity body,
			HttpParams httpParams, String method) {
		InputStream inputStream = null;
		try {
			// ֤����֤
			// ��ȡHttpClient����
			HttpClient httpClient = new DefaultHttpClient();
//			KeyStore keyStore = GetCer();
//			SSLSocketFactory socketFactory = new SSLSocketFactory(keyStore);
//			Scheme sch = new Scheme("https", socketFactory, 443);
//			httpClient.getConnectionManager().getSchemeRegistry().register(sch);

			if (method.toUpperCase().equals(HTTP_GET)) {
				// �½�HttpGet����
				HttpGet httpGet = new HttpGet(urlString);
				if (httpParams != null) {
					httpGet.setParams(httpParams);
				}
				if (headerMap != null) {
					Set<String> set = headerMap.keySet();
					String value = null;
					for (String key : set) {
						value = headerMap.get(key);
						httpGet.addHeader(key, value);
					}
				}
				HttpResponse httpResp = httpClient.execute(httpGet);
				// �ж��ǹ�����ɹ�
				if (httpResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// ��ȡ���ص���
					inputStream = httpResp.getEntity().getContent();
				} else {
					Log.i(TAG, "HttpGet��ʽ����ʧ��");
					logger.error("[HttpUtils NetCommunicateStream] " + urlString);
					logger.error("[HttpUtils NetCommunicateStream] HttpGet��ʽ����ʧ��");
				}
			} else if (method.toUpperCase().equals(HTTP_POST)) {
				// �½�HttpPost����
				HttpPost httpPost = new HttpPost(urlString);
				// post parameters
				if (httpParams != null) {
					httpPost.setParams(httpParams);
				}
				if (headerMap != null) {
					Set<String> set = headerMap.keySet();
					String value = null;
					for (String key : set) {
						value = headerMap.get(key);
						httpPost.addHeader(key, value);
					}
				}
				// Post entity
				if (body != null) {
					// ���ò���ʵ��
					httpPost.setEntity(body);
				}
				// ��ȡHttpResponseʵ��
				HttpResponse httpResp = httpClient.execute(httpPost);
				// �ж��ǹ�����ɹ�
				if (httpResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// ��ȡ���ص���
					inputStream = httpResp.getEntity().getContent();
				} else {
					Log.i(TAG, "HttpPost��ʽ����ʧ��");
					logger.error("[HttpUtils NetCommunicateStream] " + urlString);
					logger.error("[HttpUtils NetCommunicateStream] HttpPost��ʽ����ʧ��");
				}
			} else if (method.toUpperCase().equals(HTTP_PUT)) {
				// �½�HttpPut����
				HttpPut httpPut = new HttpPut(urlString);
				// put parameters
				if (httpParams != null) {
					httpPut.setParams(httpParams);
				}
				if (headerMap != null) {
					Set<String> set = headerMap.keySet();
					String value = null;
					for (String key : set) {
						value = headerMap.get(key);
						httpPut.addHeader(key, value);
					}
				}
				// Put entity
				if (body != null) {
					// ���ò���ʵ��
					httpPut.setEntity(body);
				}
				// ��ȡHttpResponseʵ��
				HttpResponse httpResp = httpClient.execute(httpPut);
				// �ж��ǹ�����ɹ�
				if (httpResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// ��ȡ���ص���
					inputStream = httpResp.getEntity().getContent();
				} else {
					Log.i(TAG, "HttpPut��ʽ����ʧ��");
					logger.error("[HttpUtils NetCommunicateStream] " + urlString);
					logger.error("[HttpUtils NetCommunicateStream] HttpPut��ʽ����ʧ��");
				}
			} else if (method.toUpperCase().equals(HTTP_DEL)) {
				// �½�HttpDelete����
				HttpDelete httpDelete = new HttpDelete(urlString);
				if (httpParams != null) {
					httpDelete.setParams(httpParams);
				}
				if (headerMap != null) {
					Set<String> set = headerMap.keySet();
					String value = null;
					for (String key : set) {
						value = headerMap.get(key);
						httpDelete.addHeader(key, value);
					}
				}
				// ��ȡHttpResponseʵ��
				HttpResponse httpResp = httpClient.execute(httpDelete);
				// �ж��ǹ�����ɹ�
				if (httpResp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// ��ȡ���ص���
					inputStream = httpResp.getEntity().getContent();
				} else {
					Log.i(TAG, "httpDelete��ʽ����ʧ��");
					logger.error("[HttpUtils NetCommunicateStream] " + urlString);
					logger.error("[HttpUtils NetCommunicateStream] httpDelete��ʽ����ʧ��");
				}
			}
		} catch (Exception e) {
			Log.i(TAG, e.toString());
			logger.error("[HttpUtils NetCommunicateStream] " + e.toString());
		}

		return inputStream;
	}

	private KeyStore GetCer() {
		try {
			// AssetManager assetManager = context.getAssets();
			// InputStream inputStream =
			// assetManager.open("client-sensoro.cer");
			// if (inputStream == null) {
			// return null;
			// }
			InputStream inputStream = null;
			if (HttpsCertificate.IS_ASSETS) {
				AssetManager assetManager = context.getAssets();
				inputStream = assetManager
						.open("client-sensoro.cer");
				if (inputStream == null) {
					return null;
				}
			} else {
				inputStream = new ByteArrayInputStream(
						HttpsCertificate.CERTIFICATE);
			}
			// ��ȡ֤��
			CertificateFactory cerFactory = CertificateFactory
					.getInstance("X.509"); //
			Certificate cer = cerFactory.generateCertificate(inputStream);
			// ����һ��֤��⣬����֤�鵼��֤���
			KeyStore keyStore = KeyStore.getInstance("PKCS12", "BC"); //
			keyStore.load(null, null);
			keyStore.setCertificateEntry("trust", cer);

			return keyStore;
		} catch (Exception e) {
			return null;
		}
	}
}
