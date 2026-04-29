package com.rpl.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.Instant;

@Entity
public class Entry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private LedgerTransaction transaction;
    @ManyToOne
    private Account account;

    private double amount;
    private Instant chargedAt;
    private Instant bookedAt;

    public Long getId() { return id; }
    public LedgerTransaction getTransaction() { return transaction; }
    public void setTransaction(LedgerTransaction transaction) { this.transaction = transaction; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public Instant getChargedAt() { return chargedAt; }
    public void setChargedAt(Instant chargedAt) { this.chargedAt = chargedAt; }
    public Instant getBookedAt() { return bookedAt; }
    public void setBookedAt(Instant bookedAt) { this.bookedAt = bookedAt; }
}
