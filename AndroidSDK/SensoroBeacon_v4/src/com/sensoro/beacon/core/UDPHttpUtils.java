package com.sensoro.beacon.core;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import android.content.Context;

class UDPHttpUtils {
	public static final String HTTP_GET = "get";
	public static final String HTTP_POST = "post";
	public static final String HTTP_PUT = "put";
	public static final String HTTP_DELETE = "delete";

	private UDPHelper udpHelper = null;
	private String path = null;
	private Map<String, String> headerMap = null;
	private Map<String, String> bodyMap = null;
	private String method = null;

	UDPHttpUtils(Context context, String ip, int port) {
		// udpHelper = new UDPHelper(context, 10053,"117.121.10.57",3000,3);
		udpHelper = new UDPHelper(context, port, ip, 3000, 3);
	}

	public String NetCommunicate(String path, Map<String, String> headerMap,
			Map<String, String> bodyMap, String method) {
		this.path = path;
		this.headerMap = headerMap;
		this.bodyMap = bodyMap;
		this.method = method;

		String result = null;
		result = getResult();
		result = parseResult(result);
		return result;
	}

	private String parseResult(String result) {
		String response = null;
		HttpResponse httpResponse = new HttpResponse();
		if (result != null) {
			httpResponse.parseResult(result);
			if (httpResponse.getStatusCode() == HttpResponse.HTTP_200) {
				response = httpResponse.getHttpBody().getResponseJson();
			}
		}
		return response;
	}

	private String getResult() {
		String requestJson = CreateHttpRequest();
		String result = null;
		try {
			byte[] sendMessage = requestJson.getBytes("UTF-8");
			byte[] receiveMessage = udpHelper.swapData(sendMessage);
			if (receiveMessage == null) {
				return result;
			}
			result = new String(receiveMessage, "UTF-8").trim();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (TimeOutException e) {
			e.printStackTrace();
		}
		return result;
	}

	private String CreateHttpRequest() {
		String request = null;
		HttpRequest httpRequest = new HttpRequest();
		httpRequest.addMothod(method);
		httpRequest.addPath(path);
		if (headerMap != null) {
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.addParam(headerMap);
			httpRequest.addHeaders(httpHeaders);
		}
		if (bodyMap != null) {
			HttpBody httpBody = new HttpBody();
			httpBody.addParam(bodyMap);
			httpRequest.addBody(httpBody);
		}
		request = httpRequest.toJson();
		return request;
	}
}
