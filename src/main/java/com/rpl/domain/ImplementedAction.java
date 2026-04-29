package com.rpl.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.time.Instant;

@Entity
public class ImplementedAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private ProposedAction proposedAction;

    private Instant actualStart;
    private String actualParty;
    private String actualLocation;

    public Long getId() { return id; }
    public ProposedAction getProposedAction() { return proposedAction; }
    public void setProposedAction(ProposedAction proposedAction) { this.proposedAction = proposedAction; }
    public Instant getActualStart() { return actualStart; }
    public void setActualStart(Instant actualStart) { this.actualStart = actualStart; }
    public String getActualParty() { return actualParty; }
    public void setActualParty(String actualParty) { this.actualParty = actualParty; }
    public String getActualLocation() { return actualLocation; }
    public void setActualLocation(String actualLocation) { this.actualLocation = actualLocation; }
}
