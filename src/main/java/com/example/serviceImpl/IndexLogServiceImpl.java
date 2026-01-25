package com.example.serviceImpl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.IndexLog;
import com.example.repository.IndexLogRepository;
import com.example.service.IndexLogService;

@Service
public class IndexLogServiceImpl implements IndexLogService{

	  @Autowired
	  private IndexLogRepository indexLogRepository;
	  
	@Override
	public String getLastIndexedTime(String serviceName) {
		Optional<IndexLog> log = indexLogRepository.findById(serviceName);
        return log.map(l -> l.getLastIndexedAt().toString()).orElse("1970-01-01T00:00:00");
	}

	@Override
	public void updateLastIndexedTime(String serviceName, LocalDateTime time) {
		  IndexLog log = new IndexLog(serviceName, time);
	        indexLogRepository.save(log);
}

	@Override
	public void resetLastIndexedTime(String serviceName) {
		// Reset last indexed time to epoch or null
        IndexLog log = new IndexLog(serviceName, LocalDateTime.of(1970, 1, 1, 0, 0));
        indexLogRepository.save(log);
    }
	
}
