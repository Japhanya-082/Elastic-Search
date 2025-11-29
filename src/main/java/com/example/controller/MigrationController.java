package com.example.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.client.AuthClient;
import com.example.client.ConsultantClient;
import com.example.client.VmsClient;
import com.example.component.SearchMapper;
import com.example.config.AuthContext;
import com.example.model.SearchDocument;
import com.example.service.SearchService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/migration")
public class MigrationController {

    @Autowired
    private ConsultantClient consultantClient;

    @Autowired
    private AuthClient authClient;
    
    @Autowired
    private VmsClient vmsClient;

    @Autowired
    private SearchService searchService;

    @Autowired
    private SearchMapper mapper;

    @GetMapping("/index-all")
    public ResponseEntity<String> indexAll(HttpServletRequest request) {

        // Capture token from incoming request
        String token = request.getHeader("Authorization");
        AuthContext.setToken(token);   // Store token for Feign

        // 1️⃣ Consultant-Service
        List<Object> consultants = consultantClient.exportAll();
        indexAllEntities(consultants);

        // 2️⃣ Auth-Service
        List<Object> authData = authClient.exportAll();
        indexAllEntities(authData);
        
     // 2️⃣ Vms-Service
        List<Object> vmsData = vmsClient.exportAll();
        indexAllEntities(vmsData);

        AuthContext.clear(); // Cleanup (important!)

        return ResponseEntity.ok("✅ All data indexed successfully");
    }

    private void indexAllEntities(List<Object> data) {
        if (data == null) return;

        for (Object obj : data) {
            try {
                SearchDocument doc = mapper.convert(obj);
                searchService.index(doc);
            } catch (Exception e) {
                System.out.println("Error indexing: " + obj.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }
}
