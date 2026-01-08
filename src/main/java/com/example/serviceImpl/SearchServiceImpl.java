package com.example.serviceImpl;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.component.SearchMapper;
import com.example.model.SearchDocument;
import com.example.service.SearchService;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import jakarta.annotation.PostConstruct;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private SearchMapper searchMapper;

    private static final String INDEX_NAME = "search_index";
    

    @PostConstruct
    public void initIndex() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(e -> e.index(INDEX_NAME))
                    .value();

            if (!exists) {
                elasticsearchClient.indices().create(c -> c
                        .index(INDEX_NAME)
                        .mappings(m -> m.properties("title", p -> p.text(t -> t))
                                .properties("description", p -> p.text(t -> t))
                                .properties("searchableText", p -> p.text(t -> t))
                                .properties("searchableTextExact", p -> p.keyword(k -> k))
                                .properties("sourceType", p -> p.keyword(k -> k))
                                .properties("sourceId", p -> p.keyword(k -> k))
                                .properties("serviceName", p -> p.keyword(k -> k))
                        )
                );
                System.out.println("Index created: " + INDEX_NAME);
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize index: " + e.getMessage());
        }
    }

    // -------------------- INDEX ----------------------- //

    @Override
    public SearchDocument index(Object entity, String serviceName, String sourceType, String sourceId) {
        SearchDocument doc = searchMapper.convert(entity, serviceName, sourceType, sourceId);
        saveToElasticsearch(doc);
        return doc;
    }

    @Override
    public SearchDocument index(SearchDocument document) {
        saveToElasticsearch(document);
        return document;
    }

    private void saveToElasticsearch(SearchDocument doc) {
        try {
            elasticsearchClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(doc.getId())
                    .document(doc)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to index document", e);
        }
    }

    // -------------------- SEARCH ----------------------- //

    @Override
    public List<SearchDocument> globalSearch(
            String keyword,
            int page,
            int size,
            List<String> services) {

        if (keyword == null || keyword.isBlank()) return List.of();

        String cleaned = keyword.trim().replaceAll("\\s+", " ");

        try {
            Query query = buildQuery(cleaned, services);
            if (query == null) return List.of();

            SearchResponse<SearchDocument> response =
                    elasticsearchClient.search(s -> s
                            .index(INDEX_NAME)
                            .query(query)
                            .from(page * size)
                            .size(size)
                    , SearchDocument.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .toList();

        } catch (Exception e) {
            System.err.println("SEARCH ERROR: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // ---------------- BULK INDEX ----------------------- //

    public void bulkIndex(List<SearchDocument> docs) {
        try {
            List<BulkOperation> operations = docs.stream()
                    .map(doc -> BulkOperation.of(b -> b
                            .index(idx -> idx.index(INDEX_NAME)
                                    .id(doc.getId())
                                    .document(doc)
                            )
                    ))
                    .toList();

            BulkResponse response = elasticsearchClient.bulk(b -> b.operations(operations));

            if (response.errors()) {
                System.err.println("Bulk indexing errors:");
                response.items().forEach(item -> {
                    if (item.error() != null) {
                        System.err.println(item.error().reason());
                    }
                });
            }

        } catch (Exception e) {
            System.err.println("BULK INDEX FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -------------------- QUERY BUILDER ------------------ //

    private Query buildQuery(String keyword, List<String> services) {

        BoolQuery.Builder bool = new BoolQuery.Builder();

        // -------- keyword logic --------
        if (keyword.toUpperCase().contains(" AND ")) {
            String[] terms = keyword.split("(?i)\\s+AND\\s+");
            for (String term : terms) {
                bool.must(buildMultiFieldQuery(term.trim()));
            }

        } else if (keyword.toUpperCase().contains(" OR ")) {
            String[] terms = keyword.split("(?i)\\s+OR\\s+");
            for (String term : terms) {
                bool.should(buildMultiFieldQuery(term.trim()));
            }

            // IMPORTANT: enforce OR behavior
            bool.minimumShouldMatch("1");

        } else {
            bool.must(buildMultiFieldQuery(keyword));
        }

        // -------- service filter --------
        if (services != null && !services.isEmpty()) {
            bool.filter(f -> f
                .terms(t -> t
                    .field("serviceName.keyword")
                    .terms(v -> v.value(
                        services.stream()
                                .map(FieldValue::of)
                                .toList()
                    ))
                )
            );
        }

        return bool.build()._toQuery();
    }

    private Query buildMultiFieldQuery(String term) {

        BoolQuery.Builder multiField = new BoolQuery.Builder();

        boolean isNumber = term.matches("\\d+");
        boolean isEmail = term.contains("@");
        boolean longPhrase = term.length() > 25;

        // Exact match for numbers & email
        if (isNumber || isEmail) {
            multiField.should(TermQuery.of(t -> t
                    .field("searchableTextExact")
                    .value(term)
            )._toQuery());
        }

        // Main text search
        if (longPhrase) {
            multiField.should(MatchQuery.of(m -> m
                    .field("searchableText")
                    .query(term)
                    .operator(Operator.And)
            )._toQuery());
        } else {
            multiField.should(MatchQuery.of(m -> m
                    .field("searchableText")
                    .query(term)
                    .operator(Operator.And)
                    .fuzziness("AUTO")
            )._toQuery());
        }

        return multiField.build()._toQuery();
    }
}



//    private String escape(String text) {
//        if (text == null) return null;
//        return text
//                .replace("\\", "\\\\")
//                .replace("+", "\\+")
//                .replace("-", "\\-")
//                .replace("=", "\\=")
//                .replace("&&", "\\&&")
//                .replace("||", "\\||")
//                .replace(">", "\\>")
//                .replace("<", "\\<")
//                .replace("!", "\\!")
//                .replace("(", "\\(")
//                .replace(")", "\\)")
//                .replace("{", "\\{")
//                .replace("}", "\\}")
//                .replace("[", "\\[")
//                .replace("]", "\\]")
//                .replace("^", "\\^")
//                .replace("\"", "\\\"")
//                .replace("~", "\\~")
//                .replace("*", "\\*")
//                .replace("?", "\\?")
//                .replace(":", "\\:")
//                .replace("#", "\\#")
//                .replace("/", "\\/");
//    }
