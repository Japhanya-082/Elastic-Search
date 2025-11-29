package com.example.component;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.model.SearchDocument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SearchMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchDocument convert(Object entity) {
        try {
            String json = objectMapper.writeValueAsString(entity);
            return new SearchDocument(
                    UUID.randomUUID().toString(),
                    entity.getClass().getSimpleName(), // title
                    json,                              // description
                    json                               // searchableText
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert entity to JSON", e);
        }
    }
}
