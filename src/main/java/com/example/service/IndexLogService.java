package com.example.service;

import java.time.LocalDateTime;

public interface IndexLogService {

	public String getLastIndexedTime(String serviceName);
	
	 public void updateLastIndexedTime(String serviceName, LocalDateTime time);
	 
	 void resetLastIndexedTime(String serviceName);
}
