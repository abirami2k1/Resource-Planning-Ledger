package com.rpl.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ResourceAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private ProposedAction action;

    @ManyToOne
    private ResourceType resourceType;

    private double quantity;
    @Enumerated(EnumType.STRING)
    private AllocationKind kind;
    private String assetId;

    public Long getId() { return id; }
    public ProposedAction getAction() { return action; }
    public void setAction(ProposedAction action) { this.action = action; }
    public ResourceType getResourceType() { return resourceType; }
    public void setResourceType(ResourceType resourceType) { this.resourceType = resourceType; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public AllocationKind getKind() { return kind; }
    public void setKind(AllocationKind kind) { this.kind = kind; }
    public String getAssetId() { return assetId; }
    public void setAssetId(String assetId) { this.assetId = assetId; }
}
