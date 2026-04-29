package com.rpl.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ResourceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private ResourceKind kind;
    private String unit;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ResourceKind getKind() { return kind; }
    public void setKind(ResourceKind kind) { this.kind = kind; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
