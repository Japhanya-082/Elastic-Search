package com.example.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Autowired private SearchMapper searchMapper;
    @Autowired private SearchServiceImpl searchServiceImpl;

    private static final int PAGE_SIZE = 500;

    @PostMapping("/all")
    public ResponseEntity<String> indexAllServices(HttpServletRequest request) {

        String token = request.getHeader("Authorization");
        if (token != null && !token.isBlank()) {
            AuthContext.setToken(token);
        }

        try {
            System.out.println("========== INDEXING STARTED ==========");

            // VMS service indexing
            indexServiceData(safeFetchWrapper(vmsClient::exportAll), "VMS-SERVICE");

            // Uncomment if requirement service indexing is needed
            // indexServiceData(safeFetchWrapper(requirementClient::exportAll), "REQUIREMENT-SERVICE");

            // OPENREQS service indexing with pagination
            System.out.println("========== OPENREQS INDEXING STARTED ==========");
            int page = 0;
            PaginatedExportResponse pageData;

            do {
                final int currentPage = page;
                pageData = this.safeFetchWrapper(
                		(Supplier<PaginatedExportResponse>) () -> 
                		openRequirementClient.exportAll("ALL", currentPage, PAGE_SIZE));
                
                if (pageData != null && !pageData.getData().isEmpty()) {
                    indexServiceData(pageData.getData(), "OPENREQS-SERVICE");
                }
                page++;
            } while (pageData != null && !pageData.getData().isEmpty());

            System.out.println("========== INDEXING COMPLETED ==========");
            return ResponseEntity.ok("✅ VMS + REQUIREMENTS + OPEN-REQS indexed successfully");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Indexing failed: " + e.getMessage());
        } finally {
            AuthContext.clear();
        }
    }

    private void indexServiceData(List<?> data, String serviceName) {

        if (data == null || data.isEmpty()) {
            System.out.println("⚠ No data found for " + serviceName);
            return;
        }

        System.out.println("→ Indexing " + serviceName + " | Count: " + data.size());

        int BULK_SIZE = 500;
        List<SearchDocument> buffer = new ArrayList<>(BULK_SIZE);

        long startTime = System.currentTimeMillis();

        for (Object item : data) {
            try {
                SearchDocument doc;

                if (item instanceof ExportWrapper wrapper) {
                    wrapper.setServiceName(serviceName);

                    wrapper.setSourceType(
                            wrapper.getSourceType().equals(serviceName)
                                    ? wrapper.getData().getClass().getSimpleName()
                                    : wrapper.getSourceType()
                    );

                    wrapper.setSourceId(ExtractIdUtil.extractId(wrapper.getData()));

                    doc = searchMapper.convert(
                            wrapper.getData(),
                            wrapper.getSourceType(),
                            wrapper.getServiceName(),
                            wrapper.getSourceId()
                    );
                } else {
                    doc = searchMapper.convert(
                            item,
                            item.getClass().getSimpleName(),
                            serviceName,
                            ExtractIdUtil.extractId(item));
                }

                buffer.add(doc);

                if (buffer.size() == BULK_SIZE) {
                    searchServiceImpl.bulkIndex(buffer);
                    buffer.clear();
                }
            } catch (Exception e) {
                System.out.println(" Failed indexing item from " + serviceName);
                e.printStackTrace();
            }
        }

        if (!buffer.isEmpty()) {
            searchServiceImpl.bulkIndex(buffer);
        }

        long endTime = System.currentTimeMillis();
        System.out.println(" Completed indexing for " + serviceName +
                " in " + ((endTime - startTime) / 1000) + " seconds");
    }

    private <T> T safeFetchWrapper(Supplier<T> fetcher) {
        try {
            return fetcher.get();
        } catch (Exception e) {
            return null;
        }
    }
}
