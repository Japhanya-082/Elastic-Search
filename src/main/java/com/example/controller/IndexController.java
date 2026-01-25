package com.example.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.builder.SearchDocumentBuilder;
import com.example.client.OpenRequirementClient;
import com.example.client.RequirementClient;
import com.example.client.VmsClient;
import com.example.component.SearchMapper;
import com.example.config.AuthContext;
import com.example.dto.ExportWrapper;
import com.example.dto.PaginatedExportResponse;
import com.example.model.SearchDocument;
import com.example.serviceImpl.SearchServiceImpl;
import com.example.util.ExtractIdUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/index")
public class IndexController {

    @Autowired private VmsClient vmsClient;
    @Autowired private OpenRequirementClient openRequirementClient;
    @Autowired private RequirementClient requirementClient;

    @Autowired private SearchServiceImpl searchService;
    @Autowired private SearchMapper searchMapper;

    private static final int PAGE_SIZE = 500;
    private static final int BULK_SIZE = 2000;

    /* ===================== FULL INDEX ===================== */
    @PostMapping("/all")
    public ResponseEntity<String> indexAllServices(HttpServletRequest request) {

        String token = request != null ? request.getHeader("Authorization") : null;
        if (token != null && !token.isBlank()) {
            AuthContext.setToken(token);
        }

        try {
            System.out.println("========== GLOBAL INDEXING STARTED ==========");

            // 1️⃣ VMS full
            indexServiceData(safeFetch(() -> vmsClient.exportAll()), "VMS-SERVICE");

            // 2️⃣ REQUIREMENT full
            indexServiceData(safeFetch(() -> requirementClient.exportAll()), "REQUIREMENT-SERVICE");

            // 3️⃣ OPENREQS full (paginated)
            indexOpenReqs();

            System.out.println("========== GLOBAL INDEXING COMPLETED ==========");
            return ResponseEntity.ok("All services indexed successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Indexing failed: " + e.getMessage());
        } finally {
            AuthContext.clear();
        }
    }

    /* ===================== INCREMENTAL INDEX ===================== */
    public void indexVmsIncremental(String lastIndexedTime) {
        System.out.println("→ Incremental indexing VMS-SERVICE since " + lastIndexedTime);
        List<ExportWrapper> data = safeFetch(() ->
                vmsClient.exportIncremental(lastIndexedTime)
        );
        indexServiceData(data, "VMS-SERVICE");
    }

    public void indexOpenReqsIncremental(String lastIndexedTime) {
        System.out.println("→ Incremental indexing OPENREQS-SERVICE since " + lastIndexedTime);

        int page = 0;
        PaginatedExportResponse response;

        do {
            final int currentPage = page;
            response = safeFetch(() ->
                    openRequirementClient.exportIncremental(lastIndexedTime, "ALL", currentPage, PAGE_SIZE)
            );

            if (response != null && response.getData() != null) {
                indexServiceData(response.getData(), "OPENREQS-SERVICE");
            }
            page++;

        } while (response != null && response.getData() != null && !response.getData().isEmpty());
    }

    /* ===================== OPENREQS FULL PAGINATION ===================== */
    private void indexOpenReqs() {
        System.out.println("========== OPENREQS FULL INDEX STARTED ==========");

        int page = 0;
        PaginatedExportResponse response;

        do {
            final int currentPage = page;
            response = safeFetch(() ->
                    openRequirementClient.exportAll("ALL", currentPage, PAGE_SIZE)
            );

            if (response != null && response.getData() != null) {
                indexServiceData(response.getData(), "OPENREQS-SERVICE");
            }
            page++;

        } while (response != null && response.getData() != null && !response.getData().isEmpty());

        System.out.println("========== OPENREQS FULL INDEX COMPLETED ==========");
    }

    /* ===================== CORE INDEX LOGIC ===================== */
    private void indexServiceData(List<?> data, String serviceName) {

        if (data == null || data.isEmpty()) {
            System.out.println("No data for " + serviceName);
            return;
        }

        System.out.println("→ Indexing " + serviceName + " | Records: " + data.size());
        List<SearchDocument> buffer = new ArrayList<>(BULK_SIZE);
        long start = System.currentTimeMillis();

        for (Object item : data) {
            try {
                Object entity;
                String sourceType;

                if (item instanceof ExportWrapper wrapper) {
                    entity = wrapper.getData();
                    sourceType = wrapper.getSourceType();
                } else {
                    entity = item;
                    sourceType = entity.getClass().getSimpleName();
                }

                SearchDocument docFromMapper = searchMapper.convert(
                        entity,
                        sourceType,
                        serviceName,
                        ExtractIdUtil.extractId(entity)
                );

                SearchDocument doc = SearchDocumentBuilder.build(
                        entity,
                        serviceName,
                        sourceType,
                        docFromMapper.getSearchableText(),
                        docFromMapper.getSearchableTextExact()
                );

                buffer.add(doc);

                if (buffer.size() == BULK_SIZE) {
                    searchService.bulkIndex(buffer);
                    buffer.clear();
                }

            } catch (Exception e) {
                System.err.println("Failed indexing record from " + serviceName);
                e.printStackTrace();
            }
        }

        if (!buffer.isEmpty()) {
            searchService.bulkIndex(buffer);
        }

        System.out.println("Completed " + serviceName +
                " in " + (System.currentTimeMillis() - start) / 1000 + "s");
    }

    /* ===================== SAFE FETCH ===================== */
    private <T> T safeFetch(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return null;
        }
    }
}
