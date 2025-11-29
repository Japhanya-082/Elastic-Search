package com.example.serviceImpl;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.SearchDocument;
import com.example.repository.SearchRepository;
import com.example.service.SearchService;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private SearchRepository searchRepository;

        private static final String INDEX_NAME = "search_index";

        @Override
        public SearchDocument index(SearchDocument doc) {
            // Generate unique ID if missing
            if (doc.getId() == null) {
                doc.setId(UUID.randomUUID().toString());
            }

            try {
                // Index document in Elasticsearch
                elasticsearchClient.index(i -> i
                        .index(INDEX_NAME)
                        .id(doc.getId())
                        .document(doc)
                );
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to index document in Elasticsearch", e);
            }

            return doc;
        }

        @Override
        public List<SearchDocument> globalSearch(String keyword, int size) {
            if (keyword == null || keyword.isBlank()) return List.of();

            String cleanedKeyword = keyword.trim().replaceAll("\\s+", " ");
            Query query;

            try {
                // AND Query
                if (cleanedKeyword.toUpperCase().contains(" AND ")) {
                    String[] terms = cleanedKeyword.split("(?i)\\s+AND\\s+");
                    BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
                    for (String term : terms) {
                        boolBuilder.must(MatchQuery.of(m -> m
                                .field("searchableText")
                                .query(term.trim())
                                .operator(Operator.And)
                                .fuzziness("AUTO")           // Fuzzy matching
                        )._toQuery());
                    }
                    query = boolBuilder.build()._toQuery();

                // OR Query
                } else if (cleanedKeyword.toUpperCase().contains(" OR ")) {
                    String[] terms = cleanedKeyword.split("(?i)\\s+OR\\s+");
                    BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
                    for (String term : terms) {
                        boolBuilder.should(MatchQuery.of(m -> m
                                .field("searchableText")
                                .query(term.trim())
                                .fuzziness("AUTO")
                        )._toQuery());
                    }
                    query = boolBuilder.build()._toQuery();

                // Single term or multi-word search
                } else {
                    query = MatchQuery.of(m -> m
                            .field("searchableText")
                            .query(cleanedKeyword)
                            .operator(Operator.And)
                            .fuzziness("AUTO")
                    )._toQuery();
                }

                // Execute search
                SearchResponse<SearchDocument> response = elasticsearchClient.search(
                        s -> s.index(INDEX_NAME)
                                .query(query)
                                .size(size),
                        SearchDocument.class
                );

                return response.hits().hits()
                        .stream()
                        .map(Hit::source)
                        .toList();

            } catch (IOException e) {
                e.printStackTrace();
                return List.of();
            }
        }
    }
