package com.rpl.manager;

import com.rpl.domain.Plan;
import com.rpl.domain.composite.PlanNode;
import com.rpl.engine.DepthFirstPlanIterator;
import com.rpl.exception.NotFoundException;
import com.rpl.resourceaccess.PlanRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReportManager {
    private final PlanRepository planRepository;

    public ReportManager(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<String> planReport(Long planId) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new NotFoundException("Plan not found"));
        List<String> lines = new ArrayList<>();
        DepthFirstPlanIterator iterator = new DepthFirstPlanIterator(plan);
        while (iterator.hasNext()) {
            PlanNode node = iterator.next();
            lines.add(node.getName() + " [" + node.getStatus() + "]");
        }
        return lines;
    }
}
