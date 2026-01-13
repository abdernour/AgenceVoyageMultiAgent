package com.agencevoyage.ontology.predicates;

import jade.content.Predicate;
import com.agencevoyage.ontology.concepts.Voyage;

public class VoyageInfo implements Predicate {
    private Voyage voyage;

    public VoyageInfo() {}

    public VoyageInfo(Voyage voyage) {
        this.voyage = voyage;
    }

    public Voyage getVoyage() { return voyage; }
    public void setVoyage(Voyage voyage) { this.voyage = voyage; }
}