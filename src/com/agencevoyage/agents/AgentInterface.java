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

import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;

public class AgentInterface extends Agent {

    private AID coordinateur;
    private Scanner scanner;
    private SimpleDateFormat dateFormat;

    protected void setup() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("🌍 WELCOME TO INTELLIGENT TRAVEL AGENCY");
        System.out.println("═".repeat(60));

        scanner = new Scanner(System.in);
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        // Find Coordinateur in Yellow Pages
        coordinateur = rechercherCoordinateur();

        if (coordinateur == null) {
            System.err.println("❌ ERROR: Coordinateur not found!");
            doDelete();
            return;
        }

        System.out.println("✅ Connected to Coordinateur: " + coordinateur.getLocalName());

        // Start user interaction
        addBehaviour(new InteractionUtilisateur());

        // Listen for responses
        addBehaviour(new RecevoirReponses());
    }

    private AID rechercherCoordinateur() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("coordinateur-voyage");
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) {
                return result[0].getName();
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Behaviour: User interaction (console input)
    private class InteractionUtilisateur extends CyclicBehaviour {

        private boolean premiereFois = true;

        public void action() {
            if (premiereFois) {
                premiereFois = false;
                demarrerRecherche();
            }
            block(5000); // Wait 5 seconds before allowing new search
        }

        private void demarrerRecherche() {
            System.out.println("\n" + "─".repeat(60));
            System.out.println("🔍 NEW TRAVEL SEARCH");
            System.out.println("─".repeat(60));

            // Get destination
            System.out.print("📍 Enter destination (Rome/Barcelona/London): ");
            String destination = scanner.nextLine().trim();
            if (destination.isEmpty()) destination = "Rome";

            // Get number of nights
            System.out.print("🌙 Number of nights: ");
            String nightsStr = scanner.nextLine().trim();
            int nights = nightsStr.isEmpty() ? 5 : Integer.parseInt(nightsStr);

            // Get budget in DA
            System.out.print("💰 Maximum budget (DA): ");
            String budgetStr = scanner.nextLine().trim();
            float budget = budgetStr.isEmpty() ? 100000.0f : Float.parseFloat(budgetStr);

            System.out.println("\n⏳ Searching for best offers...");
            System.out.println("🕐 Request sent at: " + dateFormat.format(new Date()));

            // Send REQUEST to Coordinateur
            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.addReceiver(coordinateur);
            request.setContent("destination:" + destination + ",nights:" + nights + ",budget:" + budget);
            request.setConversationId("travel-request-" + System.currentTimeMillis());
            request.setReplyWith("request" + System.currentTimeMillis());

            send(request);
        }
    }

    // Behaviour: Receive responses from Coordinateur
    private class RecevoirReponses extends CyclicBehaviour {

        public void action() {
            ACLMessage msg = receive();

            if (msg != null) {
                switch (msg.getPerformative()) {
                    case ACLMessage.AGREE:
                        System.out.println("\n✅ " + msg.getContent());
                        System.out.println("🔄 Negotiation started...\n");
                        break;

                    case ACLMessage.INFORM:
                        afficherOffre(msg.getContent());
                        break;

                    case ACLMessage.FAILURE:
                        System.out.println("\n❌ SEARCH FAILED");
                        System.out.println("   Reason: " + msg.getContent());
                        proposeNewSearch();
                        break;
                }
            } else {
                block();
            }
        }

        private void afficherOffre(String offre) {
            System.out.println("\n" + "╔" + "═".repeat(58) + "╗");
            System.out.println("║" + center(" BOOKING CONFIRMATION ", 58) + "║");
            System.out.println("╚" + "═".repeat(58) + "╝");

            if (offre.contains("BEST_OFFER|")) {
                String[] parts = offre.split("\\|");

                // Extract booking ID
                String bookingID = "";
                if (parts.length >= 5 && parts[4].contains("BookingID:")) {
                    bookingID = parts[4].replace("BookingID:", "").trim();
                }

                System.out.println("\n┌" + "─".repeat(58) + "┐");
                System.out.println("│ 🎫 BOOKING ID: " + bookingID + " ".repeat(58 - 18 - bookingID.length()) + "│");
                System.out.println("│ 📅 Date: " + dateFormat.format(new Date()) + " ".repeat(58 - 10 - dateFormat.format(new Date()).length()) + "│");
                System.out.println("└" + "─".repeat(58) + "┘");

                if (parts.length >= 3) {
                    // Extract Vol info
                    String volInfo = parts[1];
                    System.out.println("\n┌" + "─".repeat(58) + "┐");
                    System.out.println("│ ✈️  FLIGHT DETAILS:" + " ".repeat(38) + "│");
                    System.out.println("├" + "─".repeat(58) + "┤");
                    afficherDetailsVol(volInfo);
                    System.out.println("└" + "─".repeat(58) + "┘");

                    // Extract Hotel info
                    String hotelInfo = parts[2];
                    System.out.println("\n┌" + "─".repeat(58) + "┐");
                    System.out.println("│ 🏨 HOTEL DETAILS:" + " ".repeat(40) + "│");
                    System.out.println("├" + "─".repeat(58) + "┤");
                    afficherDetailsHotel(hotelInfo);
                    System.out.println("└" + "─".repeat(58) + "┘");

                    // Extract total
                    if (parts.length >= 4) {
                        String total = parts[3].replace("Total:", "").trim();
                        System.out.println("\n╔" + "═".repeat(58) + "╗");
                        System.out.println("║  TOTAL PRICE: " + total + " DA" + " ".repeat(58 - 18 - total.length() - 3) + "║");
                        System.out.println("╚" + "═".repeat(58) + "╝");
                    }
                }

                // Reservation confirmation box
                System.out.println("\n┌" + "─".repeat(58) + "┐");
                System.out.println("│" + center(" RESERVATION CONFIRMED", 58) + "│");
                System.out.println("│" + " ".repeat(58) + "│");
                System.out.println("│  Please save your booking ID for future reference.      │");

                System.out.println("└" + "─".repeat(58) + "┘");

            } else {
                System.out.println(offre);
            }

            proposeNewSearch();
        }

        private String center(String text, int width) {
            int padding = (width - text.length()) / 2;
            return " ".repeat(Math.max(0, padding)) + text + " ".repeat(Math.max(0, width - text.length() - padding));
        }

        private void afficherDetailsVol(String info) {
            String[] items = info.split(",");
            for (String item : items) {
                if (item.contains("Compagnie:")) {
                    String val = item.split("Compagnie:")[1].trim();
                    System.out.println("│   Airline: " + val + " ".repeat(58 - 13 - val.length()) + "│");
                } else if (item.contains("Vol:")) {
                    String val = item.split("Vol:")[1].trim();
                    System.out.println("│   Flight ID: " + val + " ".repeat(58 - 15 - val.length()) + "│");
                } else if (item.contains("Prix:")) {
                    String val = item.split("Prix:")[1].trim();
                    System.out.println("│   Price: " + val + " DA" + " ".repeat(58 - 10 - val.length() - 3) + "│");
                } else if (item.contains("Classe:")) {
                    String val = item.split("Classe:")[1].trim();
                    System.out.println("│   Class: " + val + " ".repeat(58 - 10 - val.length()) + "│");
                }
            }
        }

        private void afficherDetailsHotel(String info) {
            String[] items = info.split(",");
            for (String item : items) {
                if (item.contains("Nom:")) {
                    String val = item.split("Nom:")[1].trim();
                    System.out.println("│   Hotel Name: " + val + " ".repeat(58 - 15 - val.length()) + "│");
                } else if (item.contains("Hotel:")) {
                    String val = item.split("Hotel:")[1].trim();
                    System.out.println("│   Hotel ID: " + val + " ".repeat(58 - 13 - val.length()) + "│");
                } else if (item.contains("Categorie:")) {
                    String val = item.split("Categorie:")[1].trim();
                    System.out.println("│   Stars: " + val + " ".repeat(58 - 10 - val.length()) + "│");
                } else if (item.contains("PrixNuit:")) {
                    String val = item.split("PrixNuit:")[1].trim();
                    System.out.println("│   Price/night: " + val + " DA" + " ".repeat(58 - 16 - val.length() - 3) + "│");
                } else if (item.contains("Nuits:")) {
                    String val = item.split("Nuits:")[1].trim();
                    System.out.println("│   Nights: " + val + " ".repeat(58 - 11 - val.length()) + "│");
                }
            }
        }

        private void proposeNewSearch() {
            System.out.println("\n" + "─".repeat(60));
            System.out.print("🔄 New search? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (response.equals("yes") || response.equals("y")) {
                myAgent.addBehaviour(new InteractionUtilisateur());
            } else {
                System.out.println("\n" + "═".repeat(60));
                System.out.println("👋 Thank you for using our service!");
                System.out.println("   We hope you have a wonderful trip! ✈️🏨");
                System.out.println("═".repeat(60));
                myAgent.doDelete();
            }
        }
    }

    protected void takeDown() {
        if (scanner != null) {
            scanner.close();
        }
        System.out.println("👋 Agent Interface shutting down.");
    }
}