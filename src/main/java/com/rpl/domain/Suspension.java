package com.rpl.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
public class Suspension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ProposedAction proposedAction;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Instant startDate;

    private Instant endDate;

    public Long getId() {
        return id;
    }

    public ProposedAction getProposedAction() {
        return proposedAction;
    }

    public void setProposedAction(ProposedAction proposedAction) {
        this.proposedAction = proposedAction;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    /** @return null while suspension is still open */
    public Long getDurationMinutes() {
        if (endDate == null) {
            return null;
        }
        return ChronoUnit.MINUTES.between(startDate, endDate);
    }
}
