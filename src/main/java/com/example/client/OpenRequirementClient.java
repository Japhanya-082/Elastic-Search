package com.example.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.config.FeignClientConfig;
import com.example.dto.PaginatedExportResponse;

		@FeignClient(name = "OPENREQS-SERVICE", configuration = FeignClientConfig.class)
		public interface OpenRequirementClient {
		
		    @GetMapping("/openreqs/search-export/all")
		    PaginatedExportResponse exportAll(@RequestParam String entity,
		    		                          @RequestParam(defaultValue = "0") int page,
		    		                          @RequestParam(defaultValue = "20000") int size);
		}
