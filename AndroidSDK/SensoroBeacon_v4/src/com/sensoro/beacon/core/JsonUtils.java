package com.sensoro.beacon.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

class JsonUtils {

	public static String map2json(Map<?, ?> map) {
		StringBuilder json = new StringBuilder();
		json.append("{");
		if (map != null && map.size() > 0) {
			for (Object key : map.keySet()) {
				json.append(object2json(key));
				json.append(":");
				json.append(object2json(map.get(key)));
				json.append(",");
			}
			json.setCharAt(json.length() - 1, '}');
		} else {
			json.append("}");
		}
		return json.toString();
	}
	
	public static String string2json(String s) {  
        if (s == null)  
            return "";  
        StringBuilder sb = new StringBuilder();  
        for (int i = 0; i < s.length(); i++) {  
            char ch = s.charAt(i);  
            switch (ch) {  
//                case '"' :  
//                    sb.append("\\\"");  
//                    break;  
                case '\\' :  
                    sb.append("\\\\");  
                    break;  
                case '\b' :  
                    sb.append("\\b");  
                    break;  
                case '\f' :  
                    sb.append("\\f");  
                    break;  
                case '\n' :  
                    sb.append("\\n");  
                    break;  
                case '\r' :  
                    sb.append("\\r");  
                    break;  
                case '\t' :  
                    sb.append("\\t");  
                    break;  
//                case '/' :  
//                    sb.append("\\/");  
//                    break;  
                default :  
                    if (ch >= '\u0000' && ch <= '\u001F') {  
                        String ss = Integer.toHexString(ch);  
                        sb.append("\\u");  
                        for (int k = 0; k < 4 - ss.length(); k++) {  
                            sb.append('0');  
                        }  
                        sb.append(ss.toUpperCase());  
                    } else {  
                        sb.append(ch);  
                    }  
            }  
        }  
        return sb.toString();  
    }  
	
	public static String object2json(Object obj) {
		StringBuilder json = new StringBuilder();
		if (obj == null) {
			json.append("\"\"");
		} else if (obj instanceof String || obj instanceof Integer || obj instanceof Float || obj instanceof Boolean || obj instanceof Short || obj instanceof Double || obj instanceof Long || obj instanceof BigDecimal || obj instanceof BigInteger || obj instanceof Byte) {  
            if (obj instanceof String) {
				char first = ((String) obj).charAt(0);
				char end = ((String) obj).charAt(((String) obj).length() -1);
				if (first == '{' && end == '}') {
					json.append(string2json(obj.toString()));
				} else {
					json.append("\"").append(string2json(obj.toString())).append("\"");  
				}
			}
        } else if (obj instanceof Map) {
			json.append(map2json((Map<?, ?>) obj));
		} 
		return json.toString();
	}
}
