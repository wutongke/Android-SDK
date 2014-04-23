package com.sensoro.beacon.core;

import java.util.HashMap;
import java.util.Map;

class HttpRequest {
	public static final String GET = "get";
	public static final String POST = "post";
	
	private String method = null;
	private String path = null;
	private HttpHeaders httpHeaders = null;
	private HttpBody httpBody = null;

	public HttpRequest(){}
	
	public void addMothod(String method){
		this.method = method;
	}
	
	public void addPath(String path){
		this.path = path;
	}
	
	public void addHeaders(HttpHeaders httpHeaders){
		this.httpHeaders = httpHeaders;
	}
	
	public void addBody(HttpBody httpBody){
		this.httpBody = httpBody;
	}
	
	public String toJson() {
		String json = null;
		Map<String, String> httpMap = new HashMap<String, String>();
		if (method != null) {
			httpMap.put("method", method);
		}
		if (path != null) {
			httpMap.put("path", path);
		}
		if (httpHeaders != null) {
			httpMap.put("headers", httpHeaders.toRequestJson());
		}
		if (httpBody != null) {
			httpMap.put("body", httpBody.toRequestJson());
		}
		json = JsonUtils.map2json(httpMap);
		return json;
	}
}
