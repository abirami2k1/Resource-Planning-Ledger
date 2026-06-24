package com.rpl.engine;

import com.rpl.domain.ImplementedAction;
import org.springframework.stereotype.Service;

@Service
public class LedgerEntryEngine {
    private final ConsumableLedgerEntryGenerator generator;

    public LedgerEntryEngine(ConsumableLedgerEntryGenerator generator) {
        this.generator = generator;
    }

    public void generate(ImplementedAction action) {
        generator.generateEntries(action);
    }
}
