package com.rpl.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rpl.domain.composite.PlanNode;
import com.rpl.domain.composite.PlanNodeVisitor;
import com.rpl.engine.DepthFirstPlanIterator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Plan implements PlanNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @ManyToOne
    @JsonIgnore
    private Plan parentPlan;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProposedAction> actions = new ArrayList<>();

    @OneToMany(mappedBy = "parentPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plan> subPlans = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Plan getParentPlan() {
        return parentPlan;
    }

    public void setParentPlan(Plan parentPlan) {
        this.parentPlan = parentPlan;
    }

    /** Leaf actions attached directly to this plan. */
    public List<ProposedAction> getActions() {
        return actions;
    }

    public List<Plan> getSubPlans() {
        return subPlans;
    }

    /** Merged composite children (leaves first, then nested plans). */
    public List<PlanNode> getChildren() {
        List<PlanNode> merged = new ArrayList<>(actions.size() + subPlans.size());
        merged.addAll(actions);
        merged.addAll(subPlans);
        return merged;
    }

    public void addLeaf(ProposedAction a) {
        a.setPlan(this);
        actions.add(a);
    }

    public void addSubPlan(Plan sub) {
        sub.setParentPlan(this);
        subPlans.add(sub);
    }

    @Override
    public ActionStatus getStatus() {
        List<ActionStatus> leaves = new ArrayList<>();
        DepthFirstPlanIterator it = new DepthFirstPlanIterator(this);
        while (it.hasNext()) {
            PlanNode n = it.next();
            if (n instanceof ProposedAction pa) {
                leaves.add(pa.getStatus());
            }
        }
        return aggregateLeafStatuses(leaves);
    }

    @Override
    public BigDecimal getTotalAllocatedQuantity(ResourceType rt) {
        BigDecimal sum = BigDecimal.ZERO;
        DepthFirstPlanIterator it = new DepthFirstPlanIterator(this);
        while (it.hasNext()) {
            PlanNode n = it.next();
            if (n instanceof ProposedAction pa) {
                sum = sum.add(pa.getTotalAllocatedQuantity(rt));
            }
        }
        return sum;
    }

    @Override
    public void accept(PlanNodeVisitor v) {
        v.visit(this);
    }

    private static ActionStatus aggregateLeafStatuses(List<ActionStatus> leaves) {
        if (leaves.isEmpty()) {
            return ActionStatus.PROPOSED;
        }
        int n = leaves.size();
        long completed = leaves.stream().filter(s -> s == ActionStatus.COMPLETED).count();
        long abandoned = leaves.stream().filter(s -> s == ActionStatus.ABANDONED).count();
        boolean hasInProgress = leaves.stream().anyMatch(s -> s == ActionStatus.IN_PROGRESS);
        boolean hasSuspended = leaves.stream().anyMatch(s -> s == ActionStatus.SUSPENDED);
        if (completed == n) {
            return ActionStatus.COMPLETED;
        }
        if (hasInProgress || completed > 0) {
            return ActionStatus.IN_PROGRESS;
        }
        if (hasSuspended) {
            return ActionStatus.SUSPENDED;
        }
        if (abandoned == n) {
            return ActionStatus.ABANDONED;
        }
        return ActionStatus.PROPOSED;
    }
}
