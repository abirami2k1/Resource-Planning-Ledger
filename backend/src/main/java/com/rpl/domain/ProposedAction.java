package com.rpl.domain;

import com.rpl.domain.composite.PlanNode;
import com.rpl.domain.composite.PlanNodeVisitor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ProposedAction implements PlanNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String party;
    private String location;

    @Enumerated(EnumType.STRING)
    private ActionStatus status = ActionStatus.PROPOSED;

    private Instant timeRef;

    @ManyToOne
    @JsonIgnore
    private Plan plan;

    @OneToMany(mappedBy = "action", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceAllocation> allocations = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }

    public Instant getTimeRef() {
        return timeRef;
    }

    public void setTimeRef(Instant timeRef) {
        this.timeRef = timeRef;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public List<ResourceAllocation> getAllocations() {
        return allocations;
    }

    @Override
    public BigDecimal getTotalAllocatedQuantity(ResourceType rt) {
        return allocations.stream()
                .filter(a -> a.getResourceType() != null && a.getResourceType().getId().equals(rt.getId()))
                .map(a -> BigDecimal.valueOf(a.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public void accept(PlanNodeVisitor v) {
        v.visit(this);
    }
}
