package com.rpl.client;

import com.rpl.domain.AuditLogEntry;
import com.rpl.manager.AuditLogManager;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuditLogController {
    private final AuditLogManager auditLogManager;

    public AuditLogController(AuditLogManager auditLogManager) {
        this.auditLogManager = auditLogManager;
    }

    @GetMapping("/audit-log")
    public List<AuditLogEntry> auditLog() {
        return auditLogManager.getAll();
    }
}
