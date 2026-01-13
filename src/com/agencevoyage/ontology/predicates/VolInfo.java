package com.agencevoyage.ontology.predicates;

import jade.content.Predicate;
import com.agencevoyage.ontology.concepts.Vol;

public class VolInfo implements Predicate {
    private Vol vol;

    public VolInfo() {}

    public VolInfo(Vol vol) {
        this.vol = vol;
    }

    public Vol getVol() { return vol; }
    public void setVol(Vol vol) { this.vol = vol; }
}