package com.agencevoyage.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.proto.ContractNetInitiator;

import com.agencevoyage.ontology.concepts.Proposition;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.Enumeration;
import java.util.UUID;

public class AgentCoordinateur extends Agent {

    private List<Proposition> propositionsRecues;

    protected void setup() {
        System.out.println("🎯 Agent Coordinateur " + getLocalName() + " is starting...");

        propositionsRecues = new ArrayList<>();

        // Register in Yellow Pages
        enregistrerDansDF();

        // Add behaviour to receive travel requests
        addBehaviour(new RecevoirDemandeVoyage());

        System.out.println("🎯 Agent Coordinateur is ready!");
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println("🎯 Agent Coordinateur is shutting down.");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private void enregistrerDansDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("coordinateur-voyage");
        sd.setName("service-coordination");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("    ✅ Registered as Coordinateur");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    // Search for agents in DF by service type
    private AID[] rechercherAgents(String serviceType) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            AID[] agents = new AID[result.length];
            for (int i = 0; i < result.length; i++) {
                agents[i] = result[i].getName();
            }
            return agents;
        } catch (FIPAException e) {
            e.printStackTrace();
            return new AID[0];
        }
    }

    // Behaviour: Receive travel requests from Interface
    private class RecevoirDemandeVoyage extends CyclicBehaviour {

        public void action() {
            MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage request = myAgent.receive(template);

            if (request != null) {
                System.out.println("\n📨 Coordinateur received travel request from " +
                        request.getSender().getLocalName());

                // Send AGREE acknowledgment
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                agree.setContent("Request received, searching for best offers...");
                send(agree);

                // Extract travel info
                String content = request.getContent();
                String destination = extraireDestination(content);
                int nights = extraireNuits(content);
                float budget = extraireBudget(content);

                System.out.println("    📍 Destination: " + destination);
                System.out.println("    🌙 Nights: " + nights);
                System.out.println("    💰 Budget: " + budget + " DA");

                // Launch Contract-Net negotiation
                lancerNegociation(request.getSender(), destination, nights, budget);

            } else {
                block();
            }
        }

        private String extraireDestination(String content) {
            if (content.contains("destination:")) {
                return content.split("destination:")[1].split(",")[0].trim();
            }
            return "Rome";
        }

        private int extraireNuits(String content) {
            if (content.contains("nights:")) {
                String val = content.split("nights:")[1].split(",")[0].trim();
                return Integer.parseInt(val);
            }
            return 5;
        }

        private float extraireBudget(String content) {
            if (content.contains("budget:")) {
                String val = content.split("budget:")[1].split(",")[0].trim();
                return Float.parseFloat(val);
            }
            return 100000.0f;
        }
    }

    // Launch Contract-Net Protocol
    private void lancerNegociation(AID clientAgent, String destination, int nights, float budget) {
        System.out.println("\n🔄 Launching Contract-Net Protocol...");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          NEGOTIATION IN PROGRESS - PLEASE WAIT            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        // Find Vol and Hotel agents
        AID[] agentsVol = rechercherAgents("vente-vol");
        AID[] agentsHotel = rechercherAgents("vente-hotel");

        System.out.println("\n📡 Found " + agentsVol.length + " Vol agents");
        System.out.println("📡 Found " + agentsHotel.length + " Hotel agents");
        System.out.println("\n⏳ Sending CFP to all providers...\n");

        // Combine all agents
        AID[] allAgents = new AID[agentsVol.length + agentsHotel.length];
        System.arraycopy(agentsVol, 0, allAgents, 0, agentsVol.length);
        System.arraycopy(agentsHotel, 0, allAgents, agentsVol.length, agentsHotel.length);

        // Create CFP message
        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        for (AID agent : allAgents) {
            cfp.addReceiver(agent);
        }

        String cfpContent = "destination:" + destination + ",nights:" + nights + ",budget:" + budget;
        cfp.setContent(cfpContent);
        cfp.setConversationId("travel-booking-" + System.currentTimeMillis());
        cfp.setReplyWith("cfp" + System.currentTimeMillis());

        // Launch Contract-Net behaviour
        addBehaviour(new NegociationContractNet(this, cfp, clientAgent, destination, nights, budget));
    }

    // Contract-Net Initiator Behaviour
    private class NegociationContractNet extends ContractNetInitiator {

        private AID clientAgent;
        private String destination;
        private int nights;
        private float budget;

        private List<ACLMessage> propositionsVol;
        private List<ACLMessage> propositionsHotel;

        public NegociationContractNet(Agent a, ACLMessage cfp, AID client,
                                      String dest, int nts, float bdg) {
            super(a, cfp);
            this.clientAgent = client;
            this.destination = dest;
            this.nights = nts;
            this.budget = bdg;
            this.propositionsVol = new ArrayList<>();
            this.propositionsHotel = new ArrayList<>();
        }

        protected void handlePropose(ACLMessage propose, Vector acceptances) {
            String agentName = propose.getSender().getLocalName();

            // Real-time progress display
            if (agentName.toLowerCase().contains("vol")) {
                System.out.println("✈️  [FLIGHT PROPOSAL] " + agentName);
                propositionsVol.add(propose);
                float prix = extrairePrix(propose.getContent());
                System.out.println("    💵 Price: " + prix + " DA");
            } else if (agentName.toLowerCase().contains("hotel")) {
                System.out.println("🏨 [HOTEL PROPOSAL] " + agentName);
                propositionsHotel.add(propose);
                float prixTotal = extrairePrixTotal(propose.getContent());
                System.out.println("    💵 Total Price: " + prixTotal + " DA");
            }

            System.out.println("    ⏰ Received at: " + new java.util.Date());
            System.out.println();
        }

        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("❌ [REFUSED] " + refuse.getSender().getLocalName());
            System.out.println("    📝 Reason: " + refuse.getContent());
            System.out.println();
        }

        protected void handleAllResponses(Vector responses, Vector acceptances) {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║              ALL PROPOSALS RECEIVED                        ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");

            System.out.println("📊 Flight proposals: " + propositionsVol.size());
            System.out.println("📊 Hotel proposals: " + propositionsHotel.size());

            if (propositionsVol.isEmpty() || propositionsHotel.isEmpty()) {
                System.out.println("\n❌ Not enough proposals to create package");
                envoyerEchecAuClient("No complete package available");
                return;
            }

            // Display comparison table
            afficherTableauComparatif();

            // Find best combination
            ACLMessage meilleurVol = null;
            ACLMessage meilleurHotel = null;
            float meilleurPrix = Float.MAX_VALUE;

            System.out.println("\n🔍 Analyzing combinations...\n");

            for (ACLMessage vol : propositionsVol) {
                float prixVol = extrairePrix(vol.getContent());

                for (ACLMessage hotel : propositionsHotel) {
                    float prixHotel = extrairePrixTotal(hotel.getContent());
                    float prixTotal = prixVol + prixHotel;

                    if (prixTotal <= budget && prixTotal < meilleurPrix) {
                        meilleurPrix = prixTotal;
                        meilleurVol = vol;
                        meilleurHotel = hotel;
                    }
                }
            }

            if (meilleurVol == null || meilleurHotel == null) {
                System.out.println("❌ No combination within budget");
                envoyerEchecAuClient("No offers within budget of " + budget + " DA");
                return;
            }

            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                 BEST COMBINATION FOUND!                    ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");

            System.out.println("✈️  Flight: " + meilleurVol.getSender().getLocalName() +
                    " - " + extrairePrix(meilleurVol.getContent()) + " DA");
            System.out.println("🏨 Hotel: " + meilleurHotel.getSender().getLocalName() +
                    " - " + extrairePrixTotal(meilleurHotel.getContent()) + " DA");
            System.out.println("\n💰 TOTAL: " + meilleurPrix + " DA (Budget: " + budget + " DA)");
            System.out.println("💾 Savings: " + (budget - meilleurPrix) + " DA\n");

            // Send ACCEPT-PROPOSAL to selected agents
            for (Enumeration e = responses.elements(); e.hasMoreElements();) {
                ACLMessage response = (ACLMessage) e.nextElement();
                ACLMessage reply = response.createReply();

                if (response == meilleurVol || response == meilleurHotel) {
                    reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    System.out.println("✅ Accepting: " + response.getSender().getLocalName());
                } else {
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                }

                acceptances.add(reply);
            }

            // Generate booking ID and store final offer
            String bookingID = genererBookingID();
            storeOfferForClient(meilleurVol, meilleurHotel, meilleurPrix, bookingID);
        }

        private void afficherTableauComparatif() {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║             COMPARISON TABLE - ALL PROPOSALS               ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");

            // Flight proposals
            System.out.println("║  ✈️  FLIGHT PROPOSALS:                                      ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.printf("║  %-25s %-20s %-10s ║%n", "Provider", "Company", "Price (DA)");
            System.out.println("╠════════════════════════════════════════════════════════════╣");

            for (ACLMessage vol : propositionsVol) {
                String provider = vol.getSender().getLocalName();
                String company = extraireCompagnie(vol.getContent());
                float prix = extrairePrix(vol.getContent());
                System.out.printf("║  %-25s %-20s %10.2f ║%n",
                        truncate(provider, 25), truncate(company, 20), prix);
            }

            System.out.println("╠════════════════════════════════════════════════════════════╣");

            // Hotel proposals
            System.out.println("║  🏨 HOTEL PROPOSALS:                                        ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.printf("║  %-25s %-15s %-7s %-10s ║%n", "Provider", "Hotel", "Stars", "Total (DA)");
            System.out.println("╠════════════════════════════════════════════════════════════╣");

            for (ACLMessage hotel : propositionsHotel) {
                String provider = hotel.getSender().getLocalName();
                String hotelName = extraireNomHotel(hotel.getContent());
                String stars = extraireCategorie(hotel.getContent());
                float total = extrairePrixTotal(hotel.getContent());
                System.out.printf("║  %-25s %-15s %-7s %10.2f ║%n",
                        truncate(provider, 25), truncate(hotelName, 15), stars, total);
            }

            System.out.println("╚════════════════════════════════════════════════════════════╝");
        }

        private String truncate(String str, int maxLen) {
            if (str.length() <= maxLen) return str;
            return str.substring(0, maxLen - 3) + "...";
        }

        private String extraireCompagnie(String content) {
            if (content.contains("Compagnie:")) {
                return content.split("Compagnie:")[1].split(",")[0].trim();
            }
            return "N/A";
        }

        private String extraireNomHotel(String content) {
            if (content.contains("Nom:")) {
                return content.split("Nom:")[1].split(",")[0].trim();
            }
            return "N/A";
        }

        private String extraireCategorie(String content) {
            if (content.contains("Categorie:")) {
                return content.split("Categorie:")[1].split(",")[0].trim();
            }
            return "N/A";
        }

        protected void handleInform(ACLMessage inform) {
            System.out.println("✅ Received confirmation from " +
                    inform.getSender().getLocalName());
        }

        protected void handleAllResultNotifications(Vector resultNotifications) {
            System.out.println("\n✅ All confirmations received!");
            System.out.println("📦 Package confirmed and ready for client!\n");
        }

        private float extrairePrix(String content) {
            try {
                if (content.contains("Prix:")) {
                    String prix = content.split("Prix:")[1].split(",")[0].trim();
                    return Float.parseFloat(prix);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0.0f;
        }

        private float extrairePrixTotal(String content) {
            try {
                if (content.contains("Total:")) {
                    String total = content.split("Total:")[1].split(",")[0].replace("DA", "").replace("€", "").trim();
                    return Float.parseFloat(total);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0.0f;
        }

        private String genererBookingID() {
            return "BK-" + System.currentTimeMillis() + "-" +
                    UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        private void storeOfferForClient(ACLMessage vol, ACLMessage hotel, float total, String bookingID) {
            String offer = "BEST_OFFER|" + vol.getContent() + "|" + hotel.getContent() +
                    "|Total:" + total + "|BookingID:" + bookingID;
            myAgent.addBehaviour(new SendOfferToClient(clientAgent, offer));
        }

        private void envoyerEchecAuClient(String raison) {
            ACLMessage failure = new ACLMessage(ACLMessage.FAILURE);
            failure.addReceiver(clientAgent);
            failure.setContent(raison);
            myAgent.send(failure);
            System.out.println("❌ Sent failure to client: " + raison);
        }
    }

    // Simple behaviour to send offer to client
    private class SendOfferToClient extends CyclicBehaviour {
        private AID client;
        private String offer;
        private boolean sent = false;

        public SendOfferToClient(AID client, String offer) {
            this.client = client;
            this.offer = offer;
        }

        public void action() {
            if (!sent) {
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.addReceiver(client);
                inform.setContent(offer);
                myAgent.send(inform);
                sent = true;
                System.out.println("📤 Sent final offer to Interface");
            }
            myAgent.removeBehaviour(this);
        }
    }
}