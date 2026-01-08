package com.example.service;

import java.util.List;

import com.example.model.SearchDocument;

public interface SearchService {

	public SearchDocument index(Object entity, String serviceName, String sourceType, String sourceId);
	
	 // ADD THIS OVERLOAD ↓
    SearchDocument index(SearchDocument document);
    public List<SearchDocument> globalSearch(String keyword, int page, int size,  List<String> services);
}
