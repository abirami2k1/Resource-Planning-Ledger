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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetLedgerEntryGeneratorTest {

    @Mock LedgerTransactionRepository txRepo;
    @Mock EntryRepository entryRepo;
    @Mock AccountRepository accountRepo;
    @Mock PostingRuleEngine postingRuleEngine;
    @Mock AuditLogManager auditLog;
    @Mock ResourceAllocationRepository allocationRepo;

    Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));

    AssetLedgerEntryGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new AssetLedgerEntryGenerator(
                txRepo, entryRepo, accountRepo, postingRuleEngine, auditLog, fixedClock, allocationRepo);
    }

    @Test
    void validate_nullTimePeriod_throwsValidationException() {
        // Arrange
        ResourceAllocation alloc = new ResourceAllocation();
        alloc.setTimePeriodHours(null);
        // Act & Assert
        assertThrows(ValidationException.class, () -> generator.validate(List.of(alloc)));
    }

    @Test
    void validate_zeroTimePeriod_throwsValidationException() {
        // Arrange
        ResourceAllocation alloc = new ResourceAllocation();
        alloc.setTimePeriodHours(0.0);
        // Act & Assert
        assertThrows(ValidationException.class, () -> generator.validate(List.of(alloc)));
    }

    @Test
    void validate_positiveTimePeriod_doesNotThrow() {
        // Arrange
        ResourceAllocation alloc = new ResourceAllocation();
        alloc.setTimePeriodHours(8.0);
        // Act & Assert
        assertDoesNotThrow(() -> generator.validate(List.of(alloc)));
    }

    @Test
    void selectAllocations_returnsOnlySpecificAssets() {
        // Arrange
        ProposedAction pa = new ProposedAction();
        pa.setId(2L);
        ImplementedAction impl = new ImplementedAction();
        impl.setProposedAction(pa);

        ResourceType assetType = new ResourceType();
        assetType.setKind(ResourceKind.ASSET);

        ResourceType consumableType = new ResourceType();
        consumableType.setKind(ResourceKind.CONSUMABLE);

        ResourceAllocation specific = new ResourceAllocation();
        specific.setResourceType(assetType);
        specific.setKind(AllocationKind.SPECIFIC);
        specific.setQuantity(1.0);

        ResourceAllocation general = new ResourceAllocation();
        general.setResourceType(assetType);
        general.setKind(AllocationKind.GENERAL);
        general.setQuantity(1.0);

        ResourceAllocation consumable = new ResourceAllocation();
        consumable.setResourceType(consumableType);
        consumable.setKind(AllocationKind.GENERAL);
        consumable.setQuantity(1.0);

        when(allocationRepo.findByActionId(2L)).thenReturn(List.of(specific, general, consumable));

        // Act
        List<ResourceAllocation> result = generator.selectAllocations(impl);

        // Assert — only SPECIFIC asset returned
        assertEquals(1, result.size());
        assertEquals(AllocationKind.SPECIFIC, result.get(0).getKind());
    }
}
