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
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.ContentElement;

import com.agencevoyage.ontology.VoyageOntology;
import com.agencevoyage.ontology.concepts.*;
import com.agencevoyage.ontology.actions.RechercherVoyage;
import com.agencevoyage.ontology.predicates.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AgentCoordinateur extends Agent {
    private Codec codec = new SLCodec();
    private Ontology ontology = VoyageOntology.getInstance();
    private ContentManager contentManager;

    protected void setup() {
        System.out.println("Coordinateur " + getLocalName() + " starting...");

        contentManager = getContentManager();
        contentManager.registerLanguage(codec);
        contentManager.registerOntology(ontology);

        enregistrerDansDF();
        addBehaviour(new RecevoirDemandeVoyage());
        System.out.println("Coordinateur ready!");
    }

    protected void takeDown() {
        try {
            DFService.deregister(this);
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
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    private AID[] rechercherAgents(String serviceType) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            AID[] agents = new AID[result.length];
            for (int i = 0; i < result.length; i++) agents[i] = result[i].getName();
            return agents;
        } catch (FIPAException e) {
            e.printStackTrace();
            return new AID[0];
        }
    }

    private class RecevoirDemandeVoyage extends CyclicBehaviour {
        public void action() {
            MessageTemplate template = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchLanguage(codec.getName())
            );
            ACLMessage request = myAgent.receive(template);

            if (request != null) {
                System.out.println("\nCoordinateur received request from " + request.getSender().getLocalName());

                try {
                    ContentElement ce = contentManager.extractContent(request);
                    if (ce instanceof Action) {
                        Action action = (Action) ce;
                        if (action.getAction() instanceof RechercherVoyage) {
                            RechercherVoyage recherche = (RechercherVoyage) action.getAction();
                            Voyage voyage = recherche.getVoyage();

                            System.out.println("   Searching for: " + voyage.getDestination());
                            System.out.println("   " + voyage.getDateDepart() + " to " + voyage.getDateRetour());
                            System.out.println("   " + voyage.getNombreAdultes() + " adults, " +
                                    voyage.getNombreEnfants() + " children");
                            System.out.println("   " + voyage.getNombreChambres() + " rooms"); // NEW
                            System.out.println("   Budget: " + voyage.getBudgetMax() + " DA");

                            ACLMessage agree = request.createReply();
                            agree.setPerformative(ACLMessage.AGREE);
                            agree.setContent("Searching for best offers...");
                            send(agree);

                            lancerNegociation(request.getSender(), voyage, request.getConversationId());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error extracting request content");
                    e.printStackTrace();

                    ACLMessage failure = request.createReply();
                    failure.setPerformative(ACLMessage.FAILURE);
                    failure.setContent("Invalid request format");
                    send(failure);
                }
            } else {
                block();
            }
        }
    }

    private void lancerNegociation(AID clientAgent, Voyage voyage, String convId) {
        AID[] agentsVol = rechercherAgents("vente-vol");
        AID[] agentsHotel = rechercherAgents("vente-hotel");
        AID[] allAgents = new AID[agentsVol.length + agentsHotel.length];
        System.arraycopy(agentsVol, 0, allAgents, 0, agentsVol.length);
        System.arraycopy(agentsHotel, 0, allAgents, agentsVol.length, agentsHotel.length);

        ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        for (AID agent : allAgents) cfp.addReceiver(agent);
        cfp.setLanguage(codec.getName());
        cfp.setOntology(ontology.getName());
        cfp.setConversationId(convId);

        try {
            VoyageInfo voyageInfo = new VoyageInfo(voyage);
            contentManager.fillContent(cfp, voyageInfo);
            addBehaviour(new NegociationContractNet(this, cfp, clientAgent, voyage));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class NegociationContractNet extends ContractNetInitiator {
        private AID clientAgent;
        private Voyage voyage;
        private String conversationId;
        private List<ACLMessage> propositionsVol = new ArrayList<>();
        private List<ACLMessage> propositionsHotel = new ArrayList<>();

        public NegociationContractNet(Agent a, ACLMessage cfp, AID client, Voyage v) {
            super(a, cfp);
            this.clientAgent = client;
            this.voyage = v;
            this.conversationId = cfp.getConversationId();
        }

        @SuppressWarnings("unchecked")
        protected void handlePropose(ACLMessage propose, Vector acceptances) {
            String name = propose.getSender().getLocalName();
            if (name.toLowerCase().contains("vol")) {
                propositionsVol.add(propose);
            } else if (name.toLowerCase().contains("hotel")) {
                propositionsHotel.add(propose);
            }
        }

        @SuppressWarnings("unchecked")
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            System.out.println("Total responses: " + responses.size());
            System.out.println("Flight proposals: " + propositionsVol.size());
            System.out.println("Hotel proposals: " + propositionsHotel.size());

            if (propositionsVol.isEmpty() || propositionsHotel.isEmpty()) {
                envoyerEchec("No complete package available");
                return;
            }

            List<Proposition> validPropositions = new ArrayList<>();

            for (ACLMessage volMsg : propositionsVol) {
                try {
                    VolInfo volInfo = (VolInfo) contentManager.extractContent(volMsg);
                    Vol vol = volInfo.getVol();

                    for (ACLMessage hotelMsg : propositionsHotel) {
                        HotelInfo hotelInfo = (HotelInfo) contentManager.extractContent(hotelMsg);
                        Hotel hotel = hotelInfo.getHotel();

                        // UPDATED: Use explicit room count
                        float prixTotal = vol.getPrix() +
                                (hotel.getPrixParNuit() * voyage.getNombreNuits() * voyage.getNombreChambres());

                        if (prixTotal <= voyage.getBudgetMax()) {
                            Proposition prop = new Proposition();
                            prop.setVolPropose(vol);
                            prop.setHotelPropose(hotel);
                            prop.setCoutTotal(prixTotal);
                            validPropositions.add(prop);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (validPropositions.isEmpty()) {
                envoyerEchec("No offers within budget");
                return;
            }

            validPropositions.sort((p1, p2) -> Float.compare(p1.getCoutTotal(), p2.getCoutTotal()));

            // Deduplicate
            List<Proposition> uniquePropositions = new ArrayList<>();
            for (Proposition prop : validPropositions) {
                boolean isDuplicate = false;
                for (Proposition existing : uniquePropositions) {
                    if (isSameProposition(prop, existing)) {
                        isDuplicate = true;
                        System.out.println("   Duplicate found: " +
                                prop.getVolPropose().getCompagnie() + " + " +
                                prop.getHotelPropose().getNom());
                        break;
                    }
                }
                if (!isDuplicate) {
                    uniquePropositions.add(prop);
                    System.out.println("   Unique option: " +
                            prop.getVolPropose().getCompagnie() + " (" +
                            prop.getVolPropose().getIdVol() + ") + " +
                            prop.getHotelPropose().getNom() + " (" +
                            prop.getHotelPropose().getIdHotel() + ") - " +
                            voyage.getNombreChambres() + " rooms = " +
                            prop.getCoutTotal() + " DA");
                }
            }

            System.out.println("\nTotal combinations found: " + validPropositions.size());
            System.out.println("Unique options after deduplication: " + uniquePropositions.size());

            if (uniquePropositions.isEmpty()) {
                envoyerEchec("No offers within budget");
                return;
            }

            System.out.println("Cheapest: " + uniquePropositions.get(0).getCoutTotal() + " DA");
            if (uniquePropositions.size() > 1) {
                System.out.println("Most expensive: " + uniquePropositions.get(uniquePropositions.size()-1).getCoutTotal() + " DA");
            }

            validPropositions = uniquePropositions;

            // Accept/Reject proposals
            for (int i = 0; i < responses.size(); i++) {
                ACLMessage response = (ACLMessage) responses.get(i);
                ACLMessage reply = response.createReply();

                boolean isPartOfValidCombo = false;
                for (Proposition prop : validPropositions) {
                    if (isProposalInCombo(response, prop)) {
                        isPartOfValidCombo = true;
                        break;
                    }
                }

                reply.setPerformative(isPartOfValidCombo
                        ? ACLMessage.ACCEPT_PROPOSAL
                        : ACLMessage.REJECT_PROPOSAL);
                acceptances.add(reply);
            }

            envoyerOffresMultiples(validPropositions);
        }

        private boolean isProposalInCombo(ACLMessage proposal, Proposition combo) {
            try {
                if (proposal.getSender().getLocalName().toLowerCase().contains("vol")) {
                    VolInfo volInfo = (VolInfo) contentManager.extractContent(proposal);
                    Vol vol = volInfo.getVol();
                    return vol.getIdVol().equals(combo.getVolPropose().getIdVol());
                } else if (proposal.getSender().getLocalName().toLowerCase().contains("hotel")) {
                    HotelInfo hotelInfo = (HotelInfo) contentManager.extractContent(proposal);
                    Hotel hotel = hotelInfo.getHotel();
                    return hotel.getIdHotel().equals(combo.getHotelPropose().getIdHotel());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private boolean isSameProposition(Proposition p1, Proposition p2) {
            if (p1.getVolPropose() == null || p2.getVolPropose() == null) return false;
            if (p1.getHotelPropose() == null || p2.getHotelPropose() == null) return false;

            boolean sameVol = p1.getVolPropose().getIdVol().equals(p2.getVolPropose().getIdVol());
            boolean sameHotel = p1.getHotelPropose().getIdHotel().equals(p2.getHotelPropose().getIdHotel());

            return sameVol && sameHotel;
        }

        private void envoyerOffresMultiples(List<Proposition> propositions) {
            try {
                for (Proposition prop : propositions) {
                    ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                    inform.addReceiver(clientAgent);
                    inform.setLanguage(codec.getName());
                    inform.setOntology(ontology.getName());
                    inform.setConversationId(conversationId);

                    PropositionInfo propInfo = new PropositionInfo(prop);
                    contentManager.fillContent(inform, propInfo);
                    myAgent.send(inform);

                    System.out.println("   Sent offer: " + prop.getCoutTotal() + " DA");
                    Thread.sleep(50);
                }

                System.out.println("All " + propositions.size() + " offers sent to client");

            } catch (Exception e) {
                e.printStackTrace();
                envoyerEchec("Error creating response");
            }
        }

        private void envoyerEchec(String raison) {
            ACLMessage failure = new ACLMessage(ACLMessage.FAILURE);
            failure.addReceiver(clientAgent);
            failure.setContent(raison);
            myAgent.send(failure);
        }
    }
}