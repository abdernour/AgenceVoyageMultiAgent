package com.agencevoyage.ontology.concepts;

import jade.content.Concept;
import java.util.Date;

public class Voyage implements Concept {
    private String idVoyage;
    private String destination;
    private Date dateDepart;
    private Date dateRetour;
    private int nombreAdultes;
    private int nombreEnfants;
    private int nombreChambres;      // NEW: Explicit room count
    private float budgetMax;
    private Vol volReserve;
    private Hotel hotelReserve;

    // Constructeur par défaut (OBLIGATOIRE pour JADE)
    public Voyage() {}

    // Constructeur avec paramètres (UPDATED)
    public Voyage(String destination, Date dateDepart, Date dateRetour,
                  int nombreAdultes, int nombreEnfants, int nombreChambres, float budgetMax) {
        this.destination = destination;
        this.dateDepart = dateDepart;
        this.dateRetour = dateRetour;
        this.nombreAdultes = nombreAdultes;
        this.nombreEnfants = nombreEnfants;
        this.nombreChambres = nombreChambres;
        this.budgetMax = budgetMax;
    }

    // Calculate number of nights
    public int getNombreNuits() {
        if (dateDepart != null && dateRetour != null) {
            long diff = dateRetour.getTime() - dateDepart.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24));
        }
        return 0;
    }

    // Calculate total number of people
    public int getNombrePersonnes() {
        return nombreAdultes + nombreEnfants;
    }

    // Getters et Setters
    public String getIdVoyage() { return idVoyage; }
    public void setIdVoyage(String idVoyage) { this.idVoyage = idVoyage; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Date getDateDepart() { return dateDepart; }
    public void setDateDepart(Date dateDepart) { this.dateDepart = dateDepart; }

    public Date getDateRetour() { return dateRetour; }
    public void setDateRetour(Date dateRetour) { this.dateRetour = dateRetour; }

    public int getNombreAdultes() { return nombreAdultes; }
    public void setNombreAdultes(int nombreAdultes) {
        this.nombreAdultes = nombreAdultes;
    }

    public int getNombreEnfants() { return nombreEnfants; }
    public void setNombreEnfants(int nombreEnfants) {
        this.nombreEnfants = nombreEnfants;
    }

    // NEW: Room count getter/setter
    public int getNombreChambres() { return nombreChambres; }
    public void setNombreChambres(int nombreChambres) {
        this.nombreChambres = nombreChambres;
    }

    public float getBudgetMax() { return budgetMax; }
    public void setBudgetMax(float budgetMax) { this.budgetMax = budgetMax; }

    public Vol getVolReserve() { return volReserve; }
    public void setVolReserve(Vol volReserve) { this.volReserve = volReserve; }

    public Hotel getHotelReserve() { return hotelReserve; }
    public void setHotelReserve(Hotel hotelReserve) { this.hotelReserve = hotelReserve; }
}