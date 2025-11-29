package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = ElasticsearchRestClientAutoConfiguration.class)
@EnableFeignClients(basePackages =  "com.example.client")
@EnableDiscoveryClient
public class GlobalSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(GlobalSearchApplication.class, args);
	}

}
