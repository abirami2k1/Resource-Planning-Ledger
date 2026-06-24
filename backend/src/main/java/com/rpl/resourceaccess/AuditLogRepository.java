package com.rpl.resourceaccess;

import com.rpl.domain.AuditLogEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {
    List<AuditLogEntry> findAllByOrderByTimestampDesc();
}
