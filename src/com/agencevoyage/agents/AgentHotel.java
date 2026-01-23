package com.agencevoyage.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import com.agencevoyage.utils.DatabaseManager;
import com.agencevoyage.ontology.VoyageOntology;
import com.agencevoyage.ontology.concepts.*;
import com.agencevoyage.ontology.predicates.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AgentHotel extends Agent {
    private Connection dbConnection;
    private Codec codec = new SLCodec();
    private Ontology ontology = VoyageOntology.getInstance();
    private ContentManager contentManager;
    private String hotelCategory;

    protected void setup() {
        System.out.println("Agent Hotel " + getLocalName() + " is starting...");
        contentManager = getContentManager();
        contentManager.registerLanguage(codec);
        contentManager.registerOntology(ontology);

        dbConnection = DatabaseManager.getConnection();
        if (dbConnection == null) {
            System.err.println("❌ Failed to connect to database!");
            doDelete();
            return;
        }

        // Determine category from agent name
        String agentName = getLocalName().toLowerCase();
        if (agentName.contains("luxury")) {
            hotelCategory = "luxury";
        } else if (agentName.contains("budget")) {
            hotelCategory = "budget";
        } else {
            hotelCategory = null;
        }

        System.out.println("    Specializing in: " +
                (hotelCategory != null ? hotelCategory.toUpperCase() : "ALL") + " hotels");

        enregistrerDansDF();
        addBehaviour(new RepondreAuxCFP());
        System.out.println("Agent Hotel " + getLocalName() + " is ready!");
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println("Agent Hotel shutting down.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void enregistrerDansDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("vente-hotel");
        sd.setName("service-hotel-" + getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println("   Registered in Yellow Pages");
        } catch (FIPAException e) {
            System.err.println("   Failed to register");
            e.printStackTrace();
        }
    }

    // Use explicit roomsNeeded parameter
    private List<Hotel> rechercherHotelsMultiples(String city, int roomsNeeded) {
        List<Hotel> hotels = new ArrayList<>();

        String sql = "SELECT hotel_id, hotel_code, name, city, address, stars, " +
                "price_per_night, available_rooms, amenities " +
                "FROM hotels WHERE city = ? AND available_rooms >= ? AND active = TRUE ";

        // Add category filter
        if ("luxury".equals(hotelCategory)) {
            sql += "AND stars >= 4 ";
        } else if ("budget".equals(hotelCategory)) {
            sql += "AND stars <= 3 ";
        }

        sql += "ORDER BY price_per_night ASC LIMIT 2";

        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, city);
            stmt.setInt(2, roomsNeeded);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Hotel hotel = new Hotel();
                hotel.setIdHotel(String.valueOf(rs.getInt("hotel_id")));
                hotel.setNom(rs.getString("name"));
                hotel.setVille(rs.getString("city"));
                hotel.setAdresse(rs.getString("address"));
                hotel.setCategorie(rs.getInt("stars"));
                hotel.setPrixParNuit(rs.getFloat("price_per_night"));
                hotel.setChambresDisponibles(rs.getInt("available_rooms"));

                String amenitiesStr = rs.getString("amenities");
                if (amenitiesStr != null) {
                    List<String> services = new ArrayList<>();
                    amenitiesStr = amenitiesStr.replace("[", "").replace("]", "").replace("\"", "");
                    String[] items = amenitiesStr.split(",");
                    for (String item : items) {
                        services.add(item.trim());
                    }
                    hotel.setServices(services);
                }
                hotels.add(hotel);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("   ❌ Database error");
            e.printStackTrace();
        }
        return hotels;
    }

    private class RepondreAuxCFP extends CyclicBehaviour {
        public void action() {
            MessageTemplate template = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.CFP),
                    MessageTemplate.MatchLanguage(codec.getName())
            );
            ACLMessage message = myAgent.receive(template);

            if (message != null) {
                System.out.println(getLocalName() + " (" +
                        (hotelCategory != null ? hotelCategory.toUpperCase() : "ALL") +
                        ") received CFP");

                try {
                    VoyageInfo voyageInfo = (VoyageInfo) contentManager.extractContent(message);
                    Voyage voyage = voyageInfo.getVoyage();

                    // UPDATED: Use explicit room count from Voyage
                    int roomsNeeded = voyage.getNombreChambres();

                        System.out.println("   Searching for " + roomsNeeded + " rooms in " +
                            voyage.getDestination());

                    List<Hotel> hotels = rechercherHotelsMultiples(voyage.getDestination(), roomsNeeded);

                    if (hotels.isEmpty()) {
                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("No " + hotelCategory + " hotels available with " +
                                roomsNeeded + " rooms");
                        send(reply);
                        System.out.println("   No hotels available");
                    } else {
                        for (Hotel hotel : hotels) {
                            ACLMessage reply = message.createReply();
                            reply.setLanguage(codec.getName());
                            reply.setOntology(ontology.getName());
                            reply.setPerformative(ACLMessage.PROPOSE);

                            HotelInfo hotelInfo = new HotelInfo(hotel);
                            contentManager.fillContent(reply, hotelInfo);
                            send(reply);
                            System.out.println("   Proposed: " + hotel.getNom() +
                                    " (" + hotel.getCategorie() + "*) - " +
                                    roomsNeeded + " rooms available");
                        }
                    }
                } catch (Exception e) {
                    System.err.println("   Error processing CFP");
                    e.printStackTrace();
                    ACLMessage reply = message.createReply();
                    reply.setPerformative(ACLMessage.REFUSE);
                    send(reply);
                }
            } else {
                block();
            }
        }
    }
}