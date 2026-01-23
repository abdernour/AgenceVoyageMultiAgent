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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AgentVol extends Agent {
    private Connection dbConnection;
    private SimpleDateFormat dateFormat;
    private Codec codec = new SLCodec();
    private Ontology ontology = VoyageOntology.getInstance();
    private ContentManager contentManager;
    private String airlineName; //  Each agent represents ONE airline

    protected void setup() {
        System.out.println("Agent Vol " + getLocalName() + " is starting...");
        contentManager = getContentManager();
        contentManager.registerLanguage(codec);
        contentManager.registerOntology(ontology);

        dbConnection = DatabaseManager.getConnection();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (dbConnection == null) {
            System.err.println("❌ Failed to connect to database!");
            doDelete();
            return;
        }

        // ========== Determine airline from agent name ==========
        String agentName = getLocalName().toLowerCase();
        if (agentName.contains("airfrance")) {
            airlineName = "Air France";
        } else if (agentName.contains("ryanair")) {
            airlineName = "Ryanair";
        } else if (agentName.contains("easyjet")) {
            airlineName = "EasyJet";
        } else if (agentName.contains("emirates")) {
            airlineName = "Emirates";
        } else if (agentName.contains("british")) {
            airlineName = "British Airways";
        } else {
            // Default: agent can search all airlines
            airlineName = null;
        }

        System.out.println("   🏢 Representing airline: " + (airlineName != null ? airlineName : "ALL"));

        enregistrerDansDF();
        addBehaviour(new RepondreAuxCFP());
        System.out.println("Agent Vol " + getLocalName() + " is ready!");
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println("Agent Vol shutting down.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void enregistrerDansDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("vente-vol");
        sd.setName("service-vol-" + getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println("   Registered in Yellow Pages");
        } catch (FIPAException e) {
            System.err.println("   Failed to register in DF");
            e.printStackTrace();
        }
    }

    // ========== Filter by airline ==========
    private List<Vol> rechercherVolsMultiples(String destination, Date departureDate, int passengers) {
        List<Vol> vols = new ArrayList<>();

        // Build SQL with optional airline filter
        String sql = "SELECT flight_id, flight_code, airline, origin, destination, " +
                "departure_date, departure_time, arrival_time, class, base_price, available_seats " +
                "FROM flights WHERE destination = ? AND departure_date >= ? " +
                "AND departure_date <= DATE_ADD(?, INTERVAL 3 DAY) " +
                "AND available_seats >= ? AND active = TRUE ";

        // Add airline filter if this agent represents a specific airline
        if (airlineName != null) {
            sql += "AND airline = ? ";
        }

        sql += "ORDER BY departure_date ASC, base_price ASC LIMIT 2";

        try (PreparedStatement stmt = dbConnection.prepareStatement(sql)) {
            stmt.setString(1, destination);
            stmt.setDate(2, new java.sql.Date(departureDate.getTime()));
            stmt.setDate(3, new java.sql.Date(departureDate.getTime()));
            stmt.setInt(4, passengers);

            // Set airline parameter if needed
            if (airlineName != null) {
                stmt.setString(5, airlineName);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vol vol = new Vol();
                vol.setIdVol(String.valueOf(rs.getInt("flight_id")));
                vol.setCompagnie(rs.getString("airline"));
                vol.setVilleDepart(rs.getString("origin"));
                vol.setVilleArrivee(rs.getString("destination"));

                Date depDate = rs.getDate("departure_date");
                Time depTime = rs.getTime("departure_time");
                vol.setDateHeureDepart(new Date(depDate.getTime() + depTime.getTime()));

                Time arrTime = rs.getTime("arrival_time");
                vol.setDateHeureArrivee(new Date(depDate.getTime() + arrTime.getTime()));

                vol.setClasse(rs.getString("class"));
                vol.setPrix(rs.getFloat("base_price") * passengers);
                vol.setSiegesDisponibles(rs.getInt("available_seats"));
                vols.add(vol);
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("   Database error");
            e.printStackTrace();
        }
        return vols;
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
                        (airlineName != null ? airlineName : "ALL") + ") received CFP");
                try {
                    VoyageInfo voyageInfo = (VoyageInfo) contentManager.extractContent(message);
                    Voyage voyage = voyageInfo.getVoyage();

                    List<Vol> vols = rechercherVolsMultiples(
                            voyage.getDestination(),
                            voyage.getDateDepart(),
                            voyage.getNombrePersonnes()
                    );

                    if (vols.isEmpty()) {
                        ACLMessage reply = message.createReply();
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setContent("No flights available from " + airlineName);
                        send(reply);
                        System.out.println("   No flights available");
                    } else {
                        for (Vol vol : vols) {
                            ACLMessage reply = message.createReply();
                            reply.setLanguage(codec.getName());
                            reply.setOntology(ontology.getName());
                            reply.setPerformative(ACLMessage.PROPOSE);

                            VolInfo volInfo = new VolInfo(vol);
                            contentManager.fillContent(reply, volInfo);
                            send(reply);
                            System.out.println("   Proposed: " + vol.getCompagnie() +
                                    " (ID:" + vol.getIdVol() + ") - " + vol.getPrix() + " DA");
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