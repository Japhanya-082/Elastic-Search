package com.example.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.model.SearchDocument;
import com.example.service.SearchService;


@RestController
 
public class SearchController {

    @Autowired
    private SearchService serviceImpl;

    @PostMapping("/index")
    public ResponseEntity<SearchDocument> index(@RequestBody SearchDocument document) {
        SearchDocument saved = serviceImpl.index(document);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/getData")
    public ResponseEntity<List<SearchDocument>> search(@RequestParam String keyword,
    		                                           @RequestParam(required = false) List<String> services,
    		                                           @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "50") int size) {
        List<SearchDocument> results = serviceImpl.globalSearch(keyword, page, size,services);
        return ResponseEntity.ok(results);
    }
}
