package com.rpl.manager;

import com.rpl.domain.AuditLogEntry;
import com.rpl.resourceaccess.AuditLogRepository;
import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditLogManager {
    private static final Logger log = LoggerFactory.getLogger(AuditLogManager.class);

    private final AuditLogRepository auditLogRepository;
    private final Clock clock;

    public AuditLogManager(AuditLogRepository auditLogRepository, Clock clock) {
        this.auditLogRepository = auditLogRepository;
        this.clock = clock;
    }

    /** Does not throw; failures are logged only (fire-and-forget). */
    public void record(String event, Long accountId, Long entryId, Long actionId) {
        try {
            AuditLogEntry row = new AuditLogEntry();
            row.setEvent(event);
            row.setAccountId(accountId);
            row.setEntryId(entryId);
            row.setActionId(actionId);
            row.setTimestamp(clock.instant());
            auditLogRepository.save(row);
        } catch (Exception e) {
            log.warn("Audit log record failed for event {}: {}", event, e.getMessage(), e);
        }
    }

    public List<AuditLogEntry> getAll() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
