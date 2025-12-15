package com.agencevoyage.ontology.concepts;

import jade.content.Concept;
import jade.core.AID;

public class Proposition implements Concept {
    private String idProposition;
    private Vol volPropose;
    private Hotel hotelPropose;
    private float coutTotal;
    private AID agentProposeur;

    // Constructeur par défaut
    public Proposition() {}

    // Constructeur avec paramètres
    public Proposition(Vol volPropose, Hotel hotelPropose, AID agentProposeur) {
        this.volPropose = volPropose;
        this.hotelPropose = hotelPropose;
        this.agentProposeur = agentProposeur;
        this.coutTotal = calculerCoutTotal();
    }

    // Méthode utilitaire
    private float calculerCoutTotal() {
        float coutVol = (volPropose != null) ? volPropose.getPrix() : 0;
        float coutHotel = (hotelPropose != null) ? hotelPropose.getPrixParNuit() : 0;
        return coutVol + coutHotel;
    }

    // Getters et Setters
    public String getIdProposition() { return idProposition; }
    public void setIdProposition(String idProposition) {
        this.idProposition = idProposition;
    }

    public Vol getVolPropose() { return volPropose; }
    public void setVolPropose(Vol volPropose) {
        this.volPropose = volPropose;
        this.coutTotal = calculerCoutTotal();
    }

    public Hotel getHotelPropose() { return hotelPropose; }
    public void setHotelPropose(Hotel hotelPropose) {
        this.hotelPropose = hotelPropose;
        this.coutTotal = calculerCoutTotal();
    }

    public float getCoutTotal() { return coutTotal; }
    public void setCoutTotal(float coutTotal) { this.coutTotal = coutTotal; }

    public AID getAgentProposeur() { return agentProposeur; }
    public void setAgentProposeur(AID agentProposeur) {
        this.agentProposeur = agentProposeur;
    }

    @Override
    public String toString() {
        return "Proposition{" +
                "vol=" + volPropose +
                ", hotel=" + hotelPropose +
                ", coutTotal=" + coutTotal + "€" +
                '}';
    }
}