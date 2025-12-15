package com.agencevoyage.ontology;

import jade.content.onto.*;
import jade.content.schema.*;
import com.agencevoyage.ontology.concepts.*;

public class VoyageOntology extends Ontology {

    // Nom unique de l'ontologie
    public static final String ONTOLOGY_NAME = "voyage-ontology";

    // Instance singleton
    private static Ontology instance = new VoyageOntology();

    // Obtenir l'instance
    public static Ontology getInstance() {
        return instance;
    }

    // Constructeur privé
    private VoyageOntology() {
        super(ONTOLOGY_NAME, BasicOntology.getInstance());

        try {
            // Ajouter les concepts
            add(new ConceptSchema(Voyage.class.getName()), Voyage.class);
            add(new ConceptSchema(Vol.class.getName()), Vol.class);
            add(new ConceptSchema(Hotel.class.getName()), Hotel.class);
            add(new ConceptSchema(Proposition.class.getName()), Proposition.class);

            // Définir la structure de Voyage
            ConceptSchema voyageSchema = (ConceptSchema) getSchema(Voyage.class.getName());
            voyageSchema.add("idVoyage", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            voyageSchema.add("destination", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            voyageSchema.add("dateDepart", (PrimitiveSchema) getSchema(BasicOntology.DATE));
            voyageSchema.add("dateRetour", (PrimitiveSchema) getSchema(BasicOntology.DATE));
            voyageSchema.add("nombrePersonnes", (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            voyageSchema.add("budgetMax", (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            voyageSchema.add("volReserve", (ConceptSchema) getSchema(Vol.class.getName()), ObjectSchema.OPTIONAL);
            voyageSchema.add("hotelReserve", (ConceptSchema) getSchema(Hotel.class.getName()), ObjectSchema.OPTIONAL);

            // Définir la structure de Vol
            ConceptSchema volSchema = (ConceptSchema) getSchema(Vol.class.getName());
            volSchema.add("idVol", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            volSchema.add("compagnie", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            volSchema.add("villeDepart", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            volSchema.add("villeArrivee", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            volSchema.add("dateHeureDepart", (PrimitiveSchema) getSchema(BasicOntology.DATE));
            volSchema.add("dateHeureArrivee", (PrimitiveSchema) getSchema(BasicOntology.DATE));
            volSchema.add("prix", (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            volSchema.add("siegesDisponibles", (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            volSchema.add("classe", (PrimitiveSchema) getSchema(BasicOntology.STRING));

            // Définir la structure de Hotel
            ConceptSchema hotelSchema = (ConceptSchema) getSchema(Hotel.class.getName());
            hotelSchema.add("idHotel", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            hotelSchema.add("nom", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            hotelSchema.add("ville", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            hotelSchema.add("adresse", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            hotelSchema.add("categorie", (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            hotelSchema.add("prixParNuit", (PrimitiveSchema) getSchema(BasicOntology.FLOAT));
            hotelSchema.add("chambresDisponibles", (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

            // Définir la structure de Proposition
            ConceptSchema propositionSchema = (ConceptSchema) getSchema(Proposition.class.getName());
            propositionSchema.add("idProposition", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            propositionSchema.add("volPropose", (ConceptSchema) getSchema(Vol.class.getName()), ObjectSchema.OPTIONAL);
            propositionSchema.add("hotelPropose", (ConceptSchema) getSchema(Hotel.class.getName()), ObjectSchema.OPTIONAL);
            propositionSchema.add("coutTotal", (PrimitiveSchema) getSchema(BasicOntology.FLOAT));

        } catch (OntologyException e) {
            e.printStackTrace();
        }
    }
}