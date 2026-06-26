package com.rpl.engine;

import com.rpl.domain.*;
import com.rpl.exception.ValidationException;
import com.rpl.manager.AuditLogManager;
import com.rpl.resourceaccess.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumableLedgerEntryGeneratorTest {

    @Mock LedgerTransactionRepository txRepo;
    @Mock EntryRepository entryRepo;
    @Mock AccountRepository accountRepo;
    @Mock PostingRuleEngine postingRuleEngine;
    @Mock AuditLogManager auditLog;
    @Mock ResourceAllocationRepository allocationRepo;

    Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));

    ConsumableLedgerEntryGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ConsumableLedgerEntryGenerator(
                txRepo, entryRepo, accountRepo, postingRuleEngine, auditLog, fixedClock, allocationRepo);
    }

    @Test
    void validate_positiveQuantity_doesNotThrow() {
        // Arrange
        ResourceAllocation alloc = new ResourceAllocation();
        alloc.setQuantity(10.0);
        // Act & Assert — should not throw
        assertDoesNotThrow(() -> generator.validate(List.of(alloc)));
    }

    @Test
    void validate_zeroQuantity_throwsValidationException() {
        // Arrange
        ResourceAllocation alloc = new ResourceAllocation();
        alloc.setQuantity(0.0);
        // Act & Assert
        assertThrows(ValidationException.class, () -> generator.validate(List.of(alloc)));
    }

    @Test
    void validate_negativeQuantity_throwsValidationException() {
        // Arrange
        ResourceAllocation alloc = new ResourceAllocation();
        alloc.setQuantity(-5.0);
        // Act & Assert
        assertThrows(ValidationException.class, () -> generator.validate(List.of(alloc)));
    }

    @Test
    void selectAllocations_returnsOnlyConsumables() {
        // Arrange
        ProposedAction pa = new ProposedAction();
        pa.setId(1L);
        ImplementedAction impl = new ImplementedAction();
        impl.setProposedAction(pa);

        ResourceType consumable = new ResourceType();
        consumable.setKind(ResourceKind.CONSUMABLE);

        ResourceType asset = new ResourceType();
        asset.setKind(ResourceKind.ASSET);

        ResourceAllocation ca = new ResourceAllocation();
        ca.setResourceType(consumable);
        ca.setQuantity(5.0);

        ResourceAllocation aa = new ResourceAllocation();
        aa.setResourceType(asset);
        aa.setQuantity(3.0);

        when(allocationRepo.findByActionId(1L)).thenReturn(List.of(ca, aa));

        // Act
        List<ResourceAllocation> result = generator.selectAllocations(impl);

        // Assert — only consumable returned
        assertEquals(1, result.size());
        assertEquals(ResourceKind.CONSUMABLE, result.get(0).getResourceType().getKind());
    }
}
