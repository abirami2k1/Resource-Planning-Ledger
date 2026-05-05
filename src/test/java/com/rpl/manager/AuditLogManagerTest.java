package com.rpl.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rpl.domain.AuditLogEntry;
import com.rpl.resourceaccess.AuditLogRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditLogManagerTest {

    private static final Instant NOW = Instant.parse("2026-04-20T12:00:00Z");

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditLogManager auditLogManager;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        auditLogManager = new AuditLogManager(auditLogRepository, clock);
    }

    @Test
    void record_persistsRowWithClockInstant() {
        // Act
        auditLogManager.record("TEST_EVT", 1L, 2L, 3L);

        // Assert
        ArgumentCaptor<AuditLogEntry> cap = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditLogRepository).save(cap.capture());
        AuditLogEntry row = cap.getValue();
        assertEquals("TEST_EVT", row.getEvent());
        assertEquals(1L, row.getAccountId());
        assertEquals(2L, row.getEntryId());
        assertEquals(3L, row.getActionId());
        assertEquals(NOW, row.getTimestamp());
    }

    @Test
    void getAll_returnsRepositoryOrder() {
        // Arrange
        AuditLogEntry newest = new AuditLogEntry();
        when(auditLogRepository.findAllByOrderByTimestampDesc()).thenReturn(List.of(newest));

        // Act
        List<AuditLogEntry> rows = auditLogManager.getAll();

        // Assert
        assertEquals(List.of(newest), rows);
    }

    @Test
    void record_whenSaveFails_doesNotThrow() {
        when(auditLogRepository.save(any())).thenThrow(new IllegalStateException("db"));

        assertDoesNotThrow(() -> auditLogManager.record("X", null, null, null));
    }
}
