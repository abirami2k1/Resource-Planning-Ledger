package com.rpl.domain;

import com.rpl.domain.composite.PlanNode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Plan implements PlanNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProposedAction> actions = new ArrayList<>();

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<ProposedAction> getActions() { return actions; }

    @Override
    public ActionStatus getStatus() {
        if (actions.isEmpty()) {
            return ActionStatus.PROPOSED;
        }
        long completed = actions.stream().filter(a -> a.getStatus() == ActionStatus.COMPLETED).count();
        long abandoned = actions.stream().filter(a -> a.getStatus() == ActionStatus.ABANDONED).count();
        boolean hasInProgress = actions.stream().anyMatch(a -> a.getStatus() == ActionStatus.IN_PROGRESS);
        boolean hasSuspended = actions.stream().anyMatch(a -> a.getStatus() == ActionStatus.SUSPENDED);

        if (completed == actions.size()) {
            return ActionStatus.COMPLETED;
        }
        if (hasInProgress || completed > 0) {
            return ActionStatus.IN_PROGRESS;
        }
        if (hasSuspended) {
            return ActionStatus.SUSPENDED;
        }
        if (abandoned == actions.size()) {
            return ActionStatus.ABANDONED;
        }
        return ActionStatus.PROPOSED;
    }
}
