package com.rpl.client;

import com.rpl.client.dto.PlanCreateRequest;
import com.rpl.domain.Plan;
import com.rpl.manager.PlanManager;
import com.rpl.manager.ReportManager;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plans")
public class PlanController {
    private final PlanManager planManager;
    private final ReportManager reportManager;

    public PlanController(PlanManager planManager, ReportManager reportManager) {
        this.planManager = planManager;
        this.reportManager = reportManager;
    }

    @PostMapping
    public Plan create(@RequestBody PlanCreateRequest request) {
        return planManager.create(request.name(), request.protocolId(), request.parentPlanId());
    }

    @GetMapping("/{id}")
    public Plan get(@PathVariable Long id) {
        return planManager.get(id);
    }

    @GetMapping
    public List<Plan> list() {
        return planManager.list();
    }

    @GetMapping("/{id}/report")
    public List<String> report(@PathVariable Long id) {
        return reportManager.planReport(id);
    }
}
