package com.example.repository;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.example.model.SearchDocument;

public interface SearchRepository extends ElasticsearchRepository<SearchDocument, String> {
    List<SearchDocument> findBySearchableTextContaining(String keyword);
}
