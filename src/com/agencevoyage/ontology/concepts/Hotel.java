package com.agencevoyage.ontology.concepts;

import jade.content.Concept;
import java.util.ArrayList;
import java.util.List;

public class Hotel implements Concept {
    private String idHotel;
    private String nom;
    private String ville;
    private String adresse;
    private int categorie; // 1 à 5 étoiles
    private float prixParNuit;
    private int chambresDisponibles;
    private List<String> services; // wifi, piscine, parking, etc.

    // Constructeur par défaut
    public Hotel() {
        this.services = new ArrayList<>();
    }

    // Constructeur avec paramètres
    public Hotel(String idHotel, String nom, String ville, String adresse,
                 int categorie, float prixParNuit, int chambresDisponibles) {
        this.idHotel = idHotel;
        this.nom = nom;
        this.ville = ville;
        this.adresse = adresse;
        this.categorie = categorie;
        this.prixParNuit = prixParNuit;
        this.chambresDisponibles = chambresDisponibles;
        this.services = new ArrayList<>();
    }

    // Getters et Setters
    public String getIdHotel() { return idHotel; }
    public void setIdHotel(String idHotel) { this.idHotel = idHotel; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public int getCategorie() { return categorie; }
    public void setCategorie(int categorie) { this.categorie = categorie; }

    public float getPrixParNuit() { return prixParNuit; }
    public void setPrixParNuit(float prixParNuit) { this.prixParNuit = prixParNuit; }

    public int getChambresDisponibles() { return chambresDisponibles; }
    public void setChambresDisponibles(int chambresDisponibles) {
        this.chambresDisponibles = chambresDisponibles;
    }

    public List<String> getServices() { return services; }
    public void setServices(List<String> services) { this.services = services; }

    @Override
    public String toString() {
        return "Hotel{" +
                "nom='" + nom + '\'' +
                ", ville='" + ville + '\'' +
                ", categorie=" + categorie + "★" +
                ", prixParNuit=" + prixParNuit + "€" +
                '}';
    }
}