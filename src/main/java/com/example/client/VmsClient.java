package com.example.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.config.FeignClientConfig;
import com.example.dto.ExportWrapper;

@FeignClient(name = "VMS-SERVICE", configuration = FeignClientConfig.class)
public interface VmsClient {
	
	@GetMapping("/vms/search-export/all")
	List<ExportWrapper> exportAll();

	 // Incremental endpoint
    @GetMapping("/vms/search-export/incremental")
    List<ExportWrapper> exportIncremental(@RequestParam("since") String lastIndexedTime);
}
