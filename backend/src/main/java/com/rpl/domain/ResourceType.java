package com.rpl.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class ResourceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private ResourceKind kind;
    private String unit;
    /** Unit cost for ResourceCostVisitor (quantity × unitCost). */
    private Double unitCost = 0.0;

    /** Linked pool account — set automatically on creation. */
    @OneToOne
    @JsonIgnore
    private Account poolAccount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ResourceKind getKind() { return kind; }
    public void setKind(ResourceKind kind) { this.kind = kind; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getUnitCost() { return unitCost != null ? unitCost : 0.0; }
    public void setUnitCost(Double unitCost) { this.unitCost = unitCost; }
    public Account getPoolAccount() { return poolAccount; }
    public void setPoolAccount(Account poolAccount) { this.poolAccount = poolAccount; }
}
