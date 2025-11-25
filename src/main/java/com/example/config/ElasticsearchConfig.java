package com.example.config;

import org.apache.http.HttpHost; // <-- IMPORTANT: Correct import
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.repository")
public class ElasticsearchConfig {

//    @Bean
//    public ElasticsearchClient elasticsearchClient() {
//
//        RestClient restClient = RestClient.builder(
//                new HttpHost("localhost", 9200) // works with CORRECT HttpHost
//        ).build();
//
//        ElasticsearchTransport transport =
//                new RestClientTransport(restClient, new JacksonJsonpMapper());
//
//        return new ElasticsearchClient(transport);
//    }
	
	@Bean
	public ElasticsearchClient client() {
	    RestClient restClient = RestClient.builder(
	            new org.apache.http.HttpHost("localhost", 9200, "http")
	    ).build();

	    ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

	    return new ElasticsearchClient(transport);
	}

}
