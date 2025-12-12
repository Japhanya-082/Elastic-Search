package com.example.dto;

import java.util.List;

import lombok.Data;

@Data
public class PaginatedExportResponse {
    private String status;
    private int page;
    private int size;
    private int count;
    private List<ExportWrapper> data;
}

