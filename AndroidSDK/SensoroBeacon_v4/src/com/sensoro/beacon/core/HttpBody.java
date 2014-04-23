package com.sensoro.beacon.core;

import java.util.Map;

class HttpBody {
	private Map<String, String> paramMap = null;
	private String responseJson = null;
	
	HttpBody(){}
	
	public String getResponseJson() {
		return responseJson;
	}

	public void setResponseJson(String responseJson) {
		this.responseJson = responseJson;
	}
	
	public void addParam(Map<String, String> paramMap){
		this.paramMap = paramMap;
	}
	
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
