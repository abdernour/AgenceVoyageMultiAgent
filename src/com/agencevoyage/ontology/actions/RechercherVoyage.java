package com.agencevoyage.ontology.actions;

import jade.content.AgentAction;
import com.agencevoyage.ontology.concepts.Voyage;

/**
 * Action: Rechercher un voyage
 * This represents the action of searching for a trip
 */
public class RechercherVoyage implements AgentAction {
    private Voyage voyage;

    // Default constructor (required by JADE)
    public RechercherVoyage() {}

    // Constructor with parameters
    public RechercherVoyage(Voyage voyage) {
        this.voyage = voyage;
    }

    // Getters and Setters
    public Voyage getVoyage() {
        return voyage;
    }

    public void setVoyage(Voyage voyage) {
        this.voyage = voyage;
    }

    @Override
    public String toString() {
        return "RechercherVoyage{" +
                "destination=" + (voyage != null ? voyage.getDestination() : "null") +
                '}';
    }
}