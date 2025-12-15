package com.agencevoyage.ontology.concepts;

import jade.content.Concept;
import java.util.Date;

public class Voyage implements Concept {
    private String idVoyage;
    private String destination;
    private Date dateDepart;
    private Date dateRetour;
    private int nombrePersonnes;
    private float budgetMax;
    private Vol volReserve;
    private Hotel hotelReserve;

    // Constructeur par défaut (OBLIGATOIRE pour JADE)
    public Voyage() {}

    // Constructeur avec paramètres
    public Voyage(String destination, Date dateDepart, Date dateRetour,
                  int nombrePersonnes, float budgetMax) {
        this.destination = destination;
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
        this.nombrePersonnes = nombrePersonnes;
        this.budgetMax = budgetMax;
    }

    // Getters et Setters (TOUS OBLIGATOIRES pour JADE)
    public String getIdVoyage() { return idVoyage; }
    public void setIdVoyage(String idVoyage) { this.idVoyage = idVoyage; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Date getDateDepart() { return dateDepart; }
    public void setDateDepart(Date dateDepart) { this.dateDepart = dateDepart; }

    public Date getDateRetour() { return dateRetour; }
    public void setDateRetour(Date dateRetour) { this.dateRetour = dateRetour; }

    public int getNombrePersonnes() { return nombrePersonnes; }
    public void setNombrePersonnes(int nombrePersonnes) {
        this.nombrePersonnes = nombrePersonnes;
    }

    public float getBudgetMax() { return budgetMax; }
    public void setBudgetMax(float budgetMax) { this.budgetMax = budgetMax; }

    public Vol getVolReserve() { return volReserve; }
    public void setVolReserve(Vol volReserve) { this.volReserve = volReserve; }

    public Hotel getHotelReserve() { return hotelReserve; }
    public void setHotelReserve(Hotel hotelReserve) { this.hotelReserve = hotelReserve; }

    @Override
    public String toString() {
        return "Voyage{" +
                "destination='" + destination + '\'' +
                ", dateDepart=" + dateDepart +
                ", dateRetour=" + dateRetour +
                ", nombrePersonnes=" + nombrePersonnes +
                ", budgetMax=" + budgetMax +
                '}';
    }
}