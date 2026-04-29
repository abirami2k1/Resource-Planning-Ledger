package com.rpl.manager;

import com.rpl.domain.Plan;
import com.rpl.domain.ProposedAction;
import com.rpl.domain.Protocol;
import com.rpl.domain.ProtocolStep;
import com.rpl.exception.NotFoundException;
import com.rpl.resourceaccess.PlanRepository;
import com.rpl.resourceaccess.ProtocolRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlanManager {
    private final PlanRepository planRepository;
    private final ProtocolRepository protocolRepository;

    public PlanManager(PlanRepository planRepository, ProtocolRepository protocolRepository) {
        this.planRepository = planRepository;
        this.protocolRepository = protocolRepository;
    }

    public Plan create(String name, Long protocolId) {
        Plan plan = new Plan();
        plan.setName(name);
        if (protocolId != null) {
            Protocol protocol = protocolRepository.findById(protocolId)
                    .orElseThrow(() -> new NotFoundException("Protocol not found"));
            for (ProtocolStep step : protocol.getSteps()) {
                ProposedAction action = new ProposedAction();
                action.setName(step.getName());
                action.setPlan(plan);
                plan.getActions().add(action);
            }
        }
        return planRepository.save(plan);
    }

    public Plan get(Long id) {
        return planRepository.findById(id).orElseThrow(() -> new NotFoundException("Plan not found"));
    }

    public List<Plan> list() {
        return planRepository.findAll();
    }
}
