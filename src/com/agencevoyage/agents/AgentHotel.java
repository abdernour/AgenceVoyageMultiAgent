package com.agencevoyage.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import com.agencevoyage.ontology.concepts.Hotel;

import java.util.ArrayList;
import java.util.List;

public class AgentHotel extends Agent {

    // Database of available hotels
    private List<Hotel> hotels;

    // Agent setup
    protected void setup() {
        System.out.println("🏨 Agent Hotel " + getLocalName() + " is starting...");

        // Initialize hotel database
        initialiserHotels();

        // Register in Yellow Pages
        enregistrerDansDF();

        // Add behaviour to handle CFP messages
        addBehaviour(new RepondreAuxCFP());

        System.out.println("🏨 Agent Hotel " + getLocalName() + " is ready!");
        System.out.println("   Available hotels: " + hotels.size());
    }

    // Agent cleanup
    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println("🏨 Agent Hotel " + getLocalName() + " is shutting down.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    // Initialize fake hotel data (prices in DA: 1 EUR ≈ 140 DA)
    private void initialiserHotels() {
        hotels = new ArrayList<>();

        String agentName = getLocalName();

        if (agentName.contains("Luxury") || agentName.contains("1")) {
            // Luxury hotels
            hotels.add(creerHotel("H001", "Grand Hotel Colosseo", "Rome",
                    "Via del Colosseo 1", 5, 16800.0f, 20));
            hotels.add(creerHotel("H002", "Hotel Arts Barcelona", "Barcelona",
                    "Carrer de la Marina 19", 5, 21000.0f, 15));
            hotels.add(creerHotel("H003", "The Ritz London", "London",
                    "150 Piccadilly", 5, 28000.0f, 10));
        } else {
            // Budget hotels
            hotels.add(creerHotel("H101", "Rome Budget Inn", "Rome",
                    "Via Nazionale 45", 3, 7000.0f, 40));
            hotels.add(creerHotel("H102", "Barcelona Hostel", "Barcelona",
                    "Las Ramblas 20", 2, 4900.0f, 50));
            hotels.add(creerHotel("H103", "London Express", "London",
                    "Baker Street 100", 3, 8400.0f, 30));
        }
    }

    // Helper method to create a hotel
    private Hotel creerHotel(String id, String nom, String ville, String adresse,
                             int categorie, float prixNuit, int chambres) {
        Hotel hotel = new Hotel();
        hotel.setIdHotel(id);
        hotel.setNom(nom);
        hotel.setVille(ville);
        hotel.setAdresse(adresse);
        hotel.setCategorie(categorie);
        hotel.setPrixParNuit(prixNuit);
        hotel.setChambresDisponibles(chambres);

        // Add some services based on category
        List<String> services = new ArrayList<>();
        if (categorie >= 4) {
            services.add("WiFi");
            services.add("Piscine");
            services.add("Spa");
            services.add("Restaurant");
        } else if (categorie >= 3) {
            services.add("WiFi");
            services.add("Parking");
        } else {
            services.add("WiFi");
        }
        hotel.setServices(services);

        return hotel;
    }

    // Register in Directory Facilitator
    private void enregistrerDansDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("vente-hotel");
        sd.setName("service-hotel-" + getLocalName());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("   ✅ Registered in Yellow Pages");
        } catch (FIPAException e) {
            System.err.println("   ❌ Failed to register in DF");
            e.printStackTrace();
        }
    }

    // Search for hotel in destination
    private Hotel rechercherHotel(String destination) {
        for (Hotel hotel : hotels) {
            if (hotel.getVille().equalsIgnoreCase(destination) &&
                    hotel.getChambresDisponibles() > 0) {
                return hotel;
            }
        }
        return null;
    }

    // Calculate total price (price per night × nights)
    private float calculerPrixTotal(Hotel hotel, int nombreNuits) {
        return hotel.getPrixParNuit() * nombreNuits;
    }

    // Behaviour: Respond to CFP messages
    private class RepondreAuxCFP extends CyclicBehaviour {

        public void action() {
            MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage message = myAgent.receive(template);

            if (message != null) {
                System.out.println("🏨 " + getLocalName() + " received CFP from " +
                        message.getSender().getLocalName());

                // Extract destination and number of nights from message
                String[] info = extraireInformation(message.getContent());
                String destination = info[0];
                int nombreNuits = Integer.parseInt(info[1]);

                // Search for available hotel
                Hotel hotelDisponible = rechercherHotel(destination);

                // Prepare reply
                ACLMessage reply = message.createReply();

                if (hotelDisponible != null) {
                    // Hotel found - calculate total price
                    float prixTotal = calculerPrixTotal(hotelDisponible, nombreNuits);

                    // Send PROPOSE
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(creerProposition(hotelDisponible, nombreNuits, prixTotal));

                    System.out.println("   ✅ Proposing: " + hotelDisponible.getNom() +
                            " (" + hotelDisponible.getCategorie() + "★) - " +
                            prixTotal + " DA for " + nombreNuits + " nights");
                } else {
                    // No hotel - send REFUSE
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("Aucun hotel disponible à " + destination);
                    System.out.println("   ❌ No hotels in " + destination);
                }

                send(reply);

            } else {
                block();
            }
        }

        // Extract destination and nights from message
        private String[] extraireInformation(String content) {
            // Simple parsing: "destination:Rome,nights:5"
            String destination = "Rome"; // Default
            int nights = 5; // Default

            if (content != null) {
                if (content.contains("destination:")) {
                    destination = content.split("destination:")[1].split(",")[0].trim();
                }
                if (content.contains("nights:")) {
                    String nightsStr = content.split("nights:")[1].split(",")[0].trim();
                    try {
                        nights = Integer.parseInt(nightsStr);
                    } catch (NumberFormatException e) {
                        nights = 5;
                    }
                }
            }

            return new String[]{destination, String.valueOf(nights)};
        }

        // Create proposal string
        private String creerProposition(Hotel hotel, int nights, float total) {
            return "Hotel:" + hotel.getIdHotel() +
                    ",Nom:" + hotel.getNom() +
                    ",Categorie:" + hotel.getCategorie() + "★" +
                    ",PrixNuit:" + hotel.getPrixParNuit() +
                    ",Nuits:" + nights +
                    ",Total:" + total + "DA";
        }
    }
}