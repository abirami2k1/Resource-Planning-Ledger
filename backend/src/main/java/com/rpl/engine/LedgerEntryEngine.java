package com.rpl.engine;

import com.rpl.domain.ImplementedAction;
import org.springframework.stereotype.Service;

/**
 * Engine facade: generates all ledger entries on action completion or reversal.
 * ActionManager calls generate() on complete and generateReversal() on reopen.
 * Engines must not call each other.
 */
@Service
public class LedgerEntryEngine {
    private final ConsumableLedgerEntryGenerator consumableGenerator;
    private final AssetLedgerEntryGenerator assetGenerator;
    private final ReversalLedgerEntryGenerator reversalGenerator;

    public LedgerEntryEngine(ConsumableLedgerEntryGenerator consumableGenerator,
                             AssetLedgerEntryGenerator assetGenerator,
                             ReversalLedgerEntryGenerator reversalGenerator) {
        this.consumableGenerator = consumableGenerator;
        this.assetGenerator = assetGenerator;
        this.reversalGenerator = reversalGenerator;
    }

    /** Generate consumable + asset entries on action completion. */
    public void generate(ImplementedAction action) {
        consumableGenerator.generateEntries(action);
        assetGenerator.generateEntries(action);
    }

    /** Generate reversal entries to restore pool balance on reopen. */
    public void generateReversal(ImplementedAction action) {
        reversalGenerator.generateEntries(action);
    }
}
