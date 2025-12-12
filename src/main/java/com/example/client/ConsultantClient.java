package com.example.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.config.FeignClientConfig;
import com.example.dto.ExportWrapper;

@FeignClient(name = "CONSULTANT-SERVICE", configuration = FeignClientConfig.class)
public interface ConsultantClient {

    @GetMapping("/consultant/search-export/all")
    List<ExportWrapper> exportAll();
}