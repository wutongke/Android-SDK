package com.sensoro.beacon.core;

import java.util.Map;

class HttpHeaders {
	public static final String APPLICATION_JSON = "application/json";
	
	private String contentType = null;
	private String cookie = null;
	private String xPoweredBy = null;
	private String contentLength = null;
	private String date = null;
	private String connection = null;
	private String responseJson = null;
	
	private Map<String, String> paramMap = null;
	
	public String getResponseJson() {
		return responseJson;
	}

	public void setResponseJson(String responseJson) {
		this.responseJson = responseJson;
	}

	HttpHeaders(){}
	
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getxPoweredBy() {
		return xPoweredBy;
	}

	public void setxPoweredBy(String xPoweredBy) {
		this.xPoweredBy = xPoweredBy;
	}

	public String getContentLength() {
		return contentLength;
	}

	public void setContentLength(String contentLength) {
		this.contentLength = contentLength;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getConnection() {
		return connection;
	}

	public void setConnection(String connection) {
		this.connection = connection;
	}
	
	public void addParam(Map<String, String> paramMap){
		this.paramMap = paramMap;
	}

//	public String toRequestJson() {
//		String json = null;
//		Map<String, String> httpHeadersMap = new HashMap<String, String>();
//		if (contentType != null && !contentType.equals("")) {
//			httpHeadersMap.put("Content-Type", contentType);
//		}
//		if (cookie != null && !cookie.equals("")) {
//			httpHeadersMap.put("Cookie", cookie);
//		}
//		json = JsonUtils.map2json(httpHeadersMap);
//		return json;
//	}
	public String toRequestJson() {
		String json = null;
		if (paramMap == null) {
			json = "";
		} else {
			json = JsonUtils.map2json(paramMap);
		}
		return json;
	}
}
