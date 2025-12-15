package com.agencevoyage.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import com.agencevoyage.ontology.concepts.Vol;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Calendar;

public class AgentVol extends Agent {

    // Database of available flights
    private List<Vol> vols;

    // Agent setup - called when agent starts
    protected void setup() {
        System.out.println("✈️ Agent Vol " + getLocalName() + " is starting...");

        // Initialize flight database
        initialiserVols();

        // Register in the Yellow Pages (DF)
        enregistrerDansDF();

        // Add behaviour to handle CFP messages
        addBehaviour(new RepondreAuxCFP());

        System.out.println("✈️ Agent Vol " + getLocalName() + " is ready");
        System.out.println("   Available flights: " + vols.size());
    }

    // Agent cleanup - called when agent stops
    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println("✈️ Agent Vol " + getLocalName() + " is shutting down.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    // Initialize fake flight data (prices in DA: 1 EUR ≈ 140 DA)
    private void initialiserVols() {
        vols = new ArrayList<>();

        // Get agent name to differentiate data
        String agentName = getLocalName();

        if (agentName.contains("AirFrance") || agentName.contains("1")) {
            // Agent Vol 1 - Premium flights
            vols.add(creerVol("V001", "Air France", "Algiers", "Rome",
                    49000.0f, 50, "economique", 2));
            vols.add(creerVol("V002", "Air France", "Algiers", "Barcelona",
                    39200.0f, 30, "economique", 2));
            vols.add(creerVol("V003", "Air France", "Algiers", "London",
                    58800.0f, 40, "business", 3));
        } else {
            // Agent Vol 2 - Budget flights
            vols.add(creerVol("V101", "Ryanair", "Algiers", "Rome",
                    25200.0f, 80, "economique", 3));
            vols.add(creerVol("V102", "EasyJet", "Algiers", "Barcelona",
                    21000.0f, 60, "economique", 2));
            vols.add(creerVol("V103", "Vueling", "Algiers", "London",
                    28000.0f, 70, "economique", 2));
        }
    }

    // Helper method to create a flight
    private Vol creerVol(String id, String compagnie, String depart,
                         String arrivee, float prix, int sieges,
                         String classe, int joursApres) {
        Vol vol = new Vol();
        vol.setIdVol(id);
        vol.setCompagnie(compagnie);
        vol.setVilleDepart(depart);
        vol.setVilleArrivee(arrivee);
        vol.setPrix(prix);
        vol.setSiegesDisponibles(sieges);
        vol.setClasse(classe);

        // Set departure date (today + joursApres)
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, joursApres);
        vol.setDateHeureDepart(cal.getTime());

        // Set arrival date (2 hours later)
        cal.add(Calendar.HOUR, 2);
        vol.setDateHeureArrivee(cal.getTime());

        return vol;
    }

    // Register agent in the Directory Facilitator (Yellow Pages)
    private void enregistrerDansDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("vente-vol");
        sd.setName("service-vol-" + getLocalName());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("   ✅ Registered in Yellow Pages");
        } catch (FIPAException e) {
            System.err.println("   ❌ Failed to register in DF");
            e.printStackTrace();
        }
    }

    // Search for a flight matching the request
    private Vol rechercherVol(String destination) {
        for (Vol vol : vols) {
            if (vol.getVilleArrivee().equalsIgnoreCase(destination) &&
                    vol.getSiegesDisponibles() > 0) {
                return vol;
            }
        }
        return null; // No flight found
    }

    // Behaviour: Respond to CFP messages
    private class RepondreAuxCFP extends CyclicBehaviour {

        public void action() {
            // Template to receive only CFP messages
            MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage message = myAgent.receive(template);

            if (message != null) {
                // CFP received!
                System.out.println("✈️ " + getLocalName() + " received CFP from " +
                        message.getSender().getLocalName());

                // Extract destination from message content
                String destination = extraireDestination(message.getContent());

                // Search for available flight
                Vol volDisponible = rechercherVol(destination);

                // Prepare reply
                ACLMessage reply = message.createReply();

                if (volDisponible != null) {
                    // Flight found - send PROPOSE
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(creerProposition(volDisponible));
                    System.out.println("   ✅ Proposing: " + volDisponible.getCompagnie() +
                            " to " + destination + " - " + volDisponible.getPrix() + " DA");
                } else {
                    // No flight - send REFUSE
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("Aucun vol disponible pour " + destination);
                    System.out.println("   ❌ No flights to " + destination);
                }

                send(reply);

            } else {
                // No message - block until next message arrives
                block();
            }
        }

        // Extract destination from message (simple parsing)
        private String extraireDestination(String content) {
            if (content != null && content.contains("destination:")) {
                return content.split("destination:")[1].split(",")[0].trim();
            }
            return content;
        }

        // Create proposal string (simple format for now)
        private String creerProposition(Vol vol) {
            return "Vol:" + vol.getIdVol() +
                    ",Compagnie:" + vol.getCompagnie() +
                    ",Prix:" + vol.getPrix() +
                    ",Classe:" + vol.getClasse();
        }
    }
}