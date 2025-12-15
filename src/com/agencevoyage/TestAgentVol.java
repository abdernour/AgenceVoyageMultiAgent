package com.agencevoyage;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class TestAgentVol {
    public static void main(String[] args) {
        try {
            // Get JADE runtime
            Runtime rt = Runtime.instance();

            // Create profile with GUI
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "true");

            // Create main container
            AgentContainer mainContainer = rt.createMainContainer(profile);

            // Create Agent Vol 1
            AgentController agentVol1 = mainContainer.createNewAgent(
                    "AgentVol1",
                    "com.agencevoyage.agents.AgentVol",
                    new Object[0]
            );
            agentVol1.start();

            // Create Agent Vol 2
            AgentController agentVol2 = mainContainer.createNewAgent(
                    "AgentVol2",
                    "com.agencevoyage.agents.AgentVol",
                    new Object[0]
            );
            agentVol2.start();

            System.out.println(" Both Agent Vol instances are running!");
            System.out.println("Check the JADE GUI to see them.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}