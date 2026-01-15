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
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.content.ContentElement;

import com.agencevoyage.ui.SearchFrame;
import com.agencevoyage.ui.ComparisonFrame;
import com.agencevoyage.ui.ResultsFrame;
import com.agencevoyage.ui.WindowPositionManager;
import com.agencevoyage.ontology.VoyageOntology;
import com.agencevoyage.ontology.concepts.*;
import com.agencevoyage.ontology.actions.RechercherVoyage;
import com.agencevoyage.ontology.predicates.*;

import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.*;
import java.util.List;

/**
 * Main UI Agent - UPDATED to track room count
 */
public class AgentUI extends Agent {
    private AID coordinateur;
    private SearchFrame searchFrame;
    private Codec codec = new SLCodec();
    private Ontology ontology = VoyageOntology.getInstance();
    private ContentManager contentManager;
    private String currentUserEmail = null;
    private String currentUserName = null;

    private JFrame currentActiveFrame = null;

    @Override
    protected void setup() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        contentManager = getContentManager();
        contentManager.registerLanguage(codec);
        contentManager.registerOntology(ontology);

        coordinateur = findCoordinator();
        if (coordinateur == null) {
            System.err.println("ERROR: Coordinateur not found!");
        }

        SwingUtilities.invokeLater(() -> {
            searchFrame = new SearchFrame(this);
            // Calculate centered position and register it with WindowPositionManager
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle screenBounds = ge.getMaximumWindowBounds();
            int centerX = (screenBounds.width - searchFrame.getWidth()) / 2;
            int centerY = (screenBounds.height - searchFrame.getHeight()) / 2;
            WindowPositionManager.registerWindowPosition(new java.awt.Point(centerX, centerY));
            searchFrame.setVisible(true);
            currentActiveFrame = searchFrame;
        });

        addBehaviour(new ResponseHandler());
    }

    @Override
    protected void takeDown() {
        if (searchFrame != null) searchFrame.dispose();
        if (currentActiveFrame != null && currentActiveFrame != searchFrame) {
            currentActiveFrame.dispose();
        }
    }

    private AID findCoordinator() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("coordinateur-voyage");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            if (result.length > 0) return result[0].getName();
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void sendSearchRequest(Voyage voyage, String conversationId) {
        try {
            RechercherVoyage action = new RechercherVoyage();
            action.setVoyage(voyage);
            Action agentAction = new Action(coordinateur, action);

            ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
            req.addReceiver(coordinateur);
            req.setLanguage(codec.getName());
            req.setOntology(ontology.getName());
            req.setConversationId(conversationId);

            contentManager.fillContent(req, agentAction);
            send(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActiveFrame(JFrame newFrame) {
        if (currentActiveFrame != null && currentActiveFrame != searchFrame && currentActiveFrame.isVisible()) {
            currentActiveFrame.dispose();
        }
        currentActiveFrame = newFrame;
    }

    public String getCurrentUserEmail() { return currentUserEmail; }
    public void setCurrentUserEmail(String email) { this.currentUserEmail = email; }
    public String getCurrentUserName() { return currentUserName; }
    public void setCurrentUserName(String name) { this.currentUserName = name; }
    public SearchFrame getSearchFrame() { return searchFrame; }

    class ResponseHandler extends CyclicBehaviour {
        private Map<String, List<Proposition>> propositionsByConversation = new HashMap<>();
        private Map<String, java.util.Timer> timers = new HashMap<>();

        public void action() {
            MessageTemplate template = MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
            );
            ACLMessage msg = receive(template);

            if (msg != null) {
                String convId = msg.getConversationId();

                if (msg.getPerformative() == ACLMessage.INFORM) {
                    try {
                        PropositionInfo propInfo = (PropositionInfo) contentManager.extractContent(msg);
                        Proposition prop = propInfo.getProposition();

                        if (!propositionsByConversation.containsKey(convId)) {
                            propositionsByConversation.put(convId, new ArrayList<>());

                            java.util.Timer timer = new java.util.Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    showCollectedResults(convId);
                                }
                            }, 800);
                            timers.put(convId, timer);
                        }
                        propositionsByConversation.get(convId).add(prop);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (msg.getPerformative() == ACLMessage.FAILURE) {
                    SwingUtilities.invokeLater(() -> {
                        searchFrame.hideLoading();
                        JOptionPane.showMessageDialog(searchFrame, msg.getContent(),
                                "No Results", JOptionPane.WARNING_MESSAGE);
                    });
                }
            } else {
                block();
            }
        }

        private void showCollectedResults(String convId) {
            List<Proposition> allProps = propositionsByConversation.remove(convId);
            timers.remove(convId);
            if (allProps == null || allProps.isEmpty()) return;

            allProps.sort((p1, p2) -> Float.compare(p1.getCoutTotal(), p2.getCoutTotal()));

            SwingUtilities.invokeLater(() -> {
                searchFrame.hideLoading();
                Date dep = searchFrame.getDepartureDate();
                Date ret = searchFrame.getReturnDate();
                String dest = searchFrame.getDestination();
                int ad = searchFrame.getAdults();
                int ch = searchFrame.getChildren();
                int rm = searchFrame.getRooms();  // NEW: Get room count

                searchFrame.setVisible(false);

                if (allProps.size() == 1) {
                    // UPDATED: Pass rooms parameter
                    ResultsFrame rf = new ResultsFrame(AgentUI.this, allProps.get(0), dest, dep, ret, ad, ch, rm);
                    setActiveFrame(rf);
                    rf.setVisible(true);
                } else {
                    // UPDATED: Pass rooms parameter
                    ComparisonFrame cf = new ComparisonFrame(AgentUI.this, allProps, dest, dep, ret, ad, ch, rm);
                    setActiveFrame(cf);
                    cf.setVisible(true);
                }
            });
        }
    }
}