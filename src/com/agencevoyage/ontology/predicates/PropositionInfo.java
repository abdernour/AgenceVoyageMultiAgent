package com.agencevoyage.ontology.predicates;

import jade.content.Predicate;
import com.agencevoyage.ontology.concepts.Proposition;

public class PropositionInfo implements Predicate {
    private Proposition proposition;

    public PropositionInfo() {}

    public PropositionInfo(Proposition proposition) {
        this.proposition = proposition;
    }

    public Proposition getProposition() { return proposition; }
    public void setProposition(Proposition proposition) {
        this.proposition = proposition;
    }
}