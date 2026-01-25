package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_index_log")
public class IndexLog {

    @Id
    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "last_indexed_at")
    private LocalDateTime lastIndexedAt;

    // Constructors
    public IndexLog() {}

    public IndexLog(String serviceName, LocalDateTime lastIndexedAt) {
        this.serviceName = serviceName;
        this.lastIndexedAt = lastIndexedAt;
    }

    // Getters & setters
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public LocalDateTime getLastIndexedAt() { return lastIndexedAt; }
    public void setLastIndexedAt(LocalDateTime lastIndexedAt) { this.lastIndexedAt = lastIndexedAt; }
}
