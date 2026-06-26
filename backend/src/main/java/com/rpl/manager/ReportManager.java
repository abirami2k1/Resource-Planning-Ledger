package com.rpl.manager;

import com.rpl.domain.ActionStatus;
import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.composite.PlanNode;
import com.rpl.engine.DepthFirstPlanIterator;
import com.rpl.engine.FilteredPlanIterator;
import com.rpl.exception.NotFoundException;
import com.rpl.resourceaccess.PlanRepository;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportManager {
    private final PlanRepository planRepository;

    public ReportManager(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    /**
     * Depth-first plan summary report.
     * When statusFilter is provided, uses FilteredPlanIterator to show only matching nodes.
     */
    public List<String> planReport(Long planId, String statusFilter) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan not found"));
        Iterator<PlanNode> iterator;
        if (statusFilter != null && !statusFilter.isBlank()) {
            ActionStatus filter = ActionStatus.valueOf(statusFilter.toUpperCase());
            iterator = new FilteredPlanIterator(plan, node -> {
                if (node instanceof ProposedAction pa) return pa.getStatus() == filter;
                // Include composite nodes so tree structure is visible
                return true;
            });
        } else {
            iterator = new DepthFirstPlanIterator(plan);
        }
        List<String> lines = new ArrayList<>();
        while (iterator.hasNext()) {
            PlanNode node = iterator.next();
            lines.add(node.getName() + " [" + node.getStatus() + "]");
        }
        return lines;
    }

    /** Convenience overload with no filter (maintains backwards compatibility). */
    public List<String> planReport(Long planId) {
        return planReport(planId, null);
    }
}
