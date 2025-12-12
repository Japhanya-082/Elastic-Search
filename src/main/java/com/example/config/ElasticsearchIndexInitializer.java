package com.example.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ElasticsearchIndexInitializer {

    private final ElasticsearchClient elasticsearchClient;

    private static final String INDEX_NAME = "search_index";

    @PostConstruct
    public void setupIndex() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(e -> e.index(INDEX_NAME))
                    .value();

            if (!exists) {
                createIndex();
            } else {
                System.out.println(" Elasticsearch index already exists: " + INDEX_NAME);
            }

        } catch (Exception e) {
            System.out.println(" Failed checking/creating Elasticsearch index");
            e.printStackTrace();
        }
    }

    private void createIndex() {
        try {
            String mappingJson = """
            {
              "mappings": {
                "properties": {
                  "id":            { "type": "keyword" },
                  "title":         { "type": "text" },
                  "description":   { "type": "text" },
                  "searchableText": {
                    "type": "text",
                    "analyzer": "standard"
                  },
                  "searchableTextExact": { "type": "keyword" },
                  "sourceType":     { "type": "keyword" },
                  "sourceId":       { "type": "keyword" },
                  "serviceName":    { "type": "keyword" }
                }
              }
            }
            """;

            elasticsearchClient.indices().create(c -> 
                c.index(INDEX_NAME).withJson(new java.io.StringReader(mappingJson))
            );

            System.out.println(" Elasticsearch index created successfully: " + INDEX_NAME);

        } catch (Exception e) {
            System.out.println(" Failed to create Elasticsearch index");
            e.printStackTrace();
        }
    }
}
