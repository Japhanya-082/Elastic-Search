package com.example.serviceImpl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.SearchDocument;
import com.example.repository.SearchRepository;
import com.example.service.SearchService;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SearchRepository searchRepository;

    @Override
    public SearchDocument index(SearchDocument doc) {
        if(doc.getId() == null) 
        	doc.setId(UUID.randomUUID().toString());
        return searchRepository.save(doc);
    }

    @Override
    public List<SearchDocument> globalSearch(String keyword, int size) {
        return searchRepository.findBySearchableTextContaining(keyword)
                               .stream()
                               .limit(size)
                               .collect(Collectors.toList());
    }
}
