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
import com.example.serviceImpl.SearchServiceImpl;

@RestController
@RequestMapping("/global/search")
public class SearchController {

    @Autowired
    private SearchServiceImpl serviceImpl;

    @PostMapping("/index")
    public ResponseEntity<SearchDocument> index(@RequestBody SearchDocument document) {
        SearchDocument saved = serviceImpl.index(document);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/getData")
    public ResponseEntity<List<SearchDocument>> search(@RequestParam String keyword,
                                                       @RequestParam(defaultValue = "20") int size) {
        List<SearchDocument> results = serviceImpl.globalSearch(keyword, size);
        return ResponseEntity.ok(results);
    }
}
