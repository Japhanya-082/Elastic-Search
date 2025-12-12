package com.example.component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.dto.ExportWrapper;
import com.example.model.SearchDocument;
import com.fasterxml.jackson.databind.ObjectMapper; 

@Component
public class SearchMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generic conversion of any entity to SearchDocument
     * @param entity The original entity object
     * @param serviceName The service name (e.g., CONSULTANT-SERVICE)
     * @param sourceType The table/entity name (e.g., consultant_table)
     * @param sourceId The primary key / id of the entity
     * @return SearchDocument ready for Elasticsearch
     */
    public SearchDocument convert(Object entity, String sourceType,String serviceName,  String sourceId) {
        if (entity == null) return null;

        try {
            // Convert entity to JSON string
            String json = objectMapper.writeValueAsString(entity);

            // Build searchableText and searchableTextExact
            Map<String, Object> map = objectMapper.convertValue(entity, Map.class);
            StringBuilder searchableText = new StringBuilder();
            StringBuilder searchableTextExact = new StringBuilder();

            for (Object value : map.values()) {
                if (value == null) continue;
                String val = String.valueOf(value).trim();
                if (val.isEmpty()) continue;

                searchableText.append(val).append(" ");
                if (val.matches("\\d+") || val.contains("@")) {
                    searchableTextExact.append(val.toLowerCase()).append(" ");
                }
            }

            // Build the SearchDocument
            SearchDocument doc = new SearchDocument();
            doc.setId(UUID.randomUUID().toString());
            doc.setTitle(entity.getClass().getSimpleName());
            doc.setDescription(json);
            doc.setSearchableText(searchableText.toString().trim());
            doc.setSearchableTextExact(searchableTextExact.toString().trim());

            // Metadata: service, table/entity, primary key
            doc.setServiceName(serviceName != null ? serviceName : detectServiceName(entity));
            doc.setSourceType(sourceType != null ? sourceType : entity.getClass().getSimpleName());
            doc.setSourceId(sourceId != null ? sourceId : extractId(entity));

            return doc;

        } catch (Exception e) {
            throw new RuntimeException("Failed to convert entity to SearchDocument", e);
        }
    }

    /**
     * Fallback: Extract ID from entity using reflection
     */
    private String extractId(Object entity) {
        try {
            List<String> idFields = List.of("id", "consultantId", "empid", "cid"); // add more if needed
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (idFields.contains(field.getName().toLowerCase())) {
                    field.setAccessible(true);
                    Object val = field.get(entity);
                    if (val != null) return val.toString();
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Fallback: Detect service name using package name
     */
    private String detectServiceName(Object entity) {
        String pkg = entity.getClass().getPackageName().toLowerCase();
        if (pkg.contains("auth")) return "AUTH-SERVICE";
        if (pkg.contains("consultant")) return "CONSULTANT-SERVICE";
        if (pkg.contains("dashboard")) return "DASHBOARD-SERVICE";
        if (pkg.contains("gateway")) return "GATEWAY";
        if (pkg.contains("report")) return "REPORTS-SERVICE";
        if (pkg.contains("open-reqs")) return "OPENREQS-SERVICE";
        if (pkg.contains("requirements")) return "REQUIREMENT-SERVICE";
        if (pkg.contains("vms") || pkg.contains("requirement") || pkg.contains("tcvr")) return "VMS-SERVICE";
        return "UNKNOWN-SERVICE";
    }

    /**
     * Optional helper for ExportWrapper
     */
    public SearchDocument convert(ExportWrapper wrapper) {
        if (wrapper == null || wrapper.getData() == null) return null;

        return convert(
                wrapper.getData(),
                wrapper.getServiceName(),
                wrapper.getSourceType(),
                wrapper.getSourceId()
        );
    }
}



//@Component
//public class SearchMapper {
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public SearchDocument convert(Object entity) {
//        try {
//            String json = objectMapper.writeValueAsString(entity);
//            return new SearchDocument(
//                    UUID.randomUUID().toString(),
//                    entity.getClass().getSimpleName(), // title
//                    json,                              // description
//                    json                               // searchableText
//            );
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("Failed to convert entity to JSON", e);
//        }
//    }
//}
