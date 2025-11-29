package com.example.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.config.FeignClientConfig;

@FeignClient(name = "VMS-SERVICE", configuration = FeignClientConfig.class)
public interface VmsClient {
	
	@GetMapping("/vms/search-export/all")
	List<Object> exportAll();

}
