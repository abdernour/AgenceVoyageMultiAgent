package com.agencevoyage.ontology.concepts;

import jade.content.Concept;
import java.util.Date;

public class Vol implements Concept {
    private String idVol;
    private String compagnie;
    private String villeDepart;
    private String villeArrivee;
    private Date dateHeureDepart;
    private Date dateHeureArrivee;
    private float prix;
    private int siegesDisponibles;
    private String classe; // "economique", "business", "premiere"

    // Constructeur par défaut
    public Vol() {}

    // Constructeur avec paramètres
    public Vol(String idVol, String compagnie, String villeDepart,
               String villeArrivee, Date dateHeureDepart, Date dateHeureArrivee,
               float prix, int siegesDisponibles, String classe) {
        this.idVol = idVol;
        this.compagnie = compagnie;
        this.villeDepart = villeDepart;
        this.villeArrivee = villeArrivee;
        this.dateHeureDepart = dateHeureDepart;
        this.dateHeureArrivee = dateHeureArrivee;
        this.prix = prix;
        this.siegesDisponibles = siegesDisponibles;
        this.classe = classe;
    }

    // Getters et Setters
    public String getIdVol() { return idVol; }
    public void setIdVol(String idVol) { this.idVol = idVol; }

    public String getCompagnie() { return compagnie; }
    public void setCompagnie(String compagnie) { this.compagnie = compagnie; }

    public String getVilleDepart() { return villeDepart; }
    public void setVilleDepart(String villeDepart) { this.villeDepart = villeDepart; }

    public String getVilleArrivee() { return villeArrivee; }
    public void setVilleArrivee(String villeArrivee) { this.villeArrivee = villeArrivee; }

    public Date getDateHeureDepart() { return dateHeureDepart; }
    public void setDateHeureDepart(Date dateHeureDepart) {
        this.dateHeureDepart = dateHeureDepart;
    }

    public Date getDateHeureArrivee() { return dateHeureArrivee; }
    public void setDateHeureArrivee(Date dateHeureArrivee) {
        this.dateHeureArrivee = dateHeureArrivee;
    }

    public float getPrix() { return prix; }
    public void setPrix(float prix) { this.prix = prix; }

    public int getSiegesDisponibles() { return siegesDisponibles; }
    public void setSiegesDisponibles(int siegesDisponibles) {
        this.siegesDisponibles = siegesDisponibles;
    }

    public String getClasse() { return classe; }
    public void setClasse(String classe) { this.classe = classe; }

    @Override
    public String toString() {
        return "Vol{" +
                "compagnie='" + compagnie + '\'' +
                ", " + villeDepart + " -> " + villeArrivee +
                ", prix=" + prix + "€" +
                '}';
    }
}