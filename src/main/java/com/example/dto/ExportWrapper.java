package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExportWrapper {
    private Object data;
    private String sourceType;
    private String serviceName;
    private String sourceId;
}