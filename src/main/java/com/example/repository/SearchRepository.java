package com.example.repository;

import com.example.model.SearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;


import java.util.List;

public interface SearchRepository extends ElasticsearchRepository<SearchDocument, String> {
    List<SearchDocument> findBySearchableTextContaining(String keyword);
}
