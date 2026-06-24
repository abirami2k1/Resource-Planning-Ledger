package com.rpl.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class PostingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Account triggerAccount;

    @ManyToOne(optional = false)
    private Account outputAccount;

    private String strategyType;

    public Long getId() {
        return id;
    }

    public Account getTriggerAccount() {
        return triggerAccount;
    }

    public void setTriggerAccount(Account triggerAccount) {
        this.triggerAccount = triggerAccount;
    }

    public Account getOutputAccount() {
        return outputAccount;
    }

    public void setOutputAccount(Account outputAccount) {
        this.outputAccount = outputAccount;
    }

    public String getStrategyType() {
        return strategyType;
    }

    public void setStrategyType(String strategyType) {
        this.strategyType = strategyType;
    }
}
