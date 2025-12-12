package com.example.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
	
	public class ExtractIdUtil {
	
	    private static final String[] POSSIBLE_FIELDS = {
	            "id", "consultantid", "consultantId", "cid", "eid", "commentId", "consultantid",
	            "empid", "hid", "recid", "vmsid", "tblReqId",
	            "reqid", "userid", "requirementid"
	    };
	
	    private static final String[] POSSIBLE_GETTERS = {
	            "getId", "getConsultantId", "getConsultantid", "getCid",
	            "getEmpid", "getHid", "getRecid", "getVmsid",
	            "getReqid", "getUserid", "getRequirementid"
	    };
	
	    public static String extractId(Object entity) {
	
	        if (entity == null) return null;
	
	        try {
	            // 1️⃣ If entity is a Map
	            if (entity instanceof Map<?, ?> map) {
	                for (String key : POSSIBLE_FIELDS) {
	                    if (map.containsKey(key) && map.get(key) != null)
	                        return map.get(key).toString();
	                }
	            }
	
	            // 2️⃣ Try getter methods
	            for (String getter : POSSIBLE_GETTERS) {
	                try {
	                    Method m = entity.getClass().getMethod(getter);
	                    Object value = m.invoke(entity);
	                    if (value != null) return value.toString();
	                } catch (NoSuchMethodException ignored) {}
	            }
	
	            // 3️⃣ Try direct fields
	            for (String fieldName : POSSIBLE_FIELDS) {
	                try {
	                    Field f = entity.getClass().getDeclaredField(fieldName);
	                    f.setAccessible(true);
	                    Object value = f.get(entity);
	                    if (value != null) return value.toString();
	                } catch (NoSuchFieldException ignored) {}
	            }
	
	        } catch (Exception ignored) {}
	
	        return null;
	    }
	}
