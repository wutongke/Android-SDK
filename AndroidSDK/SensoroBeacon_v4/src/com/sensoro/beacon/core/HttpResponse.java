package com.sensoro.beacon.core;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

class HttpResponse {
	public static final int HTTP_200 = 200;
	public static final int HTTP_404 = 404;
	
	private int statusCode = 0;
	private HttpHeaders httpHeaders = null;
	private HttpBody httpBody = null;
	private String headers = null;
	private String body = null;
	
	private JSONObject headersJsonObject = null;
	
	HttpResponse(){
		httpHeaders = new HttpHeaders();
		httpBody = new HttpBody();
	}
	
	public int getStatusCode(){
		return statusCode;
	}
	
	public HttpHeaders getHttpHeaders(){
		return httpHeaders;
	}
	
	public HttpBody getHttpBody(){
		return httpBody;
	}
	
	public void parseResult(String result){
		JSONTokener jsonTokener = new JSONTokener(result);
		try {
			JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
			statusCode = jsonObject.getInt("statusCode");
			
			headers =  jsonObject.getString("headers");
			headersJsonObject = jsonObject.getJSONObject("headers");
			httpHeaders.setxPoweredBy(headersJsonObject.getString("x-powered-by"));
			httpHeaders.setContentType(headersJsonObject.getString("content-type"));
			httpHeaders.setContentLength(headersJsonObject.getString("content-length"));
			httpHeaders.setDate(headersJsonObject.getString("date"));
			httpHeaders.setConnection(headersJsonObject.getString("connection"));
			httpHeaders.setResponseJson(headers);
			
			body = jsonObject.getString("body");
			httpBody.setResponseJson(body);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
