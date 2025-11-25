package com.example.service;

import java.util.List;

import com.example.model.SearchDocument;

public interface SearchService {

	 SearchDocument index(SearchDocument doc);
	    List<SearchDocument> globalSearch(String keyword, int size);
}
