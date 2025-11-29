package com.example.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import feign.codec.Decoder;
import feign.jackson.JacksonDecoder;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.example.repository")
public class ElasticsearchConfig {

    /**
     * Elasticsearch High-Level Client configuration
     */
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ).build();

        ElasticsearchTransport transport =
                new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }

    /**
     * Custom Feign JSON Decoder
     * Increases JSON nesting depth limit to prevent:
     * StreamConstraintsException: Depth (1001) exceeds max allowed depth (1000)
     */
    @Bean
    public Decoder feignDecoder() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder()
                        .maxNestingDepth(5000)  // Jackson < 2.15 uses this
                        .build()
        );

        return new JacksonDecoder(mapper);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.postConfigurer(objectMapper -> {
            objectMapper.getFactory().setStreamReadConstraints(
                    StreamReadConstraints.builder()
                            .maxNestingDepth(5000)   // <--- correct for Jackson 2.13.x
                            .build()
            );
        });
    }

}
