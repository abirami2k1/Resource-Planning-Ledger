package com.rpl.manager;

import com.rpl.client.dto.PlanMetricsResponse;
import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.Protocol;
import com.rpl.domain.ProtocolStep;
import com.rpl.domain.composite.PlanNode;
import com.rpl.domain.visitor.CompletionRatioVisitor;
import com.rpl.domain.visitor.ResourceCostVisitor;
import com.rpl.domain.visitor.RiskScoreVisitor;
import com.rpl.exception.NotFoundException;
import com.rpl.resourceaccess.PlanRepository;
import com.rpl.resourceaccess.ProposedActionRepository;
import com.rpl.resourceaccess.ProtocolRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlanManager {
    private final PlanRepository planRepository;
    private final ProtocolRepository protocolRepository;
    private final ProposedActionRepository proposedActionRepository;

    public PlanManager(PlanRepository planRepository,
                       ProtocolRepository protocolRepository,
                       ProposedActionRepository proposedActionRepository) {
        this.planRepository = planRepository;
        this.protocolRepository = protocolRepository;
        this.proposedActionRepository = proposedActionRepository;
    }

    public Plan create(String name, Long protocolId, Long parentPlanId) {
        Plan plan = new Plan();
        plan.setName(name);
        if (protocolId != null) {
            Protocol protocol = protocolRepository.findById(protocolId)
                    .orElseThrow(() -> new NotFoundException("Protocol not found"));
            plan.setSourceProtocol(protocol);
            for (ProtocolStep step : protocol.getSteps()) {
                ProposedAction action = new ProposedAction();
                action.setName(step.getName());
                action.setProtocol(protocol);
                plan.addLeaf(action);
            }
        }
        if (parentPlanId != null) {
            Plan parent = planRepository.findById(parentPlanId)
                    .orElseThrow(() -> new NotFoundException("Parent plan not found"));
            parent.addSubPlan(plan);
            planRepository.save(parent);
            return plan;
        }
        return planRepository.save(plan);
    }

    public Plan get(Long id) {
        return planRepository.findById(id).orElseThrow(() -> new NotFoundException("Plan not found"));
    }

    public List<Plan> list() {
        return planRepository.findAll();
    }

    /**
     * Compute metrics for any node (plan or action) using the Visitor pattern.
     * Finds the node by searching the plan tree.
     */
    public PlanMetricsResponse metrics(Long nodeId) {
        // Try as a Plan first, then as a leaf action within any plan
        Plan plan = planRepository.findById(nodeId).orElse(null);
        PlanNode target = plan;
        if (target == null) {
            ProposedAction action = proposedActionRepository.findById(nodeId)
                    .orElseThrow(() -> new NotFoundException("Plan node not found: " + nodeId));
            target = action;
        }
        CompletionRatioVisitor crv = new CompletionRatioVisitor();
        ResourceCostVisitor rcv = new ResourceCostVisitor();
        RiskScoreVisitor rsv = new RiskScoreVisitor();
        target.accept(crv);
        target.accept(rcv);
        target.accept(rsv);
        return new PlanMetricsResponse(
                crv.getRatio(),
                crv.getTotalLeaves(),
                crv.getCompletedLeaves(),
                rcv.getTotalCost(),
                rsv.getScore());
    }
}
