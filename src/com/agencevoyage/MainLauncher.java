package com.agencevoyage;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class MainLauncher {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();

            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "true");

            AgentContainer mainContainer = rt.createMainContainer(profile);

            System.out.println("🚀 Starting Intelligent Travel Agency System...\n");

            // Create Coordinateur
            System.out.println("Creating Coordinateur...");
            AgentController coord = mainContainer.createNewAgent(
                    "Coordinateur",
                    "com.agencevoyage.agents.AgentCoordinateur",
                    new Object[0]
            );
            coord.start();
            Thread.sleep(500);

            // Create Vol Agents
            System.out.println("Creating Vol agents...");
            AgentController volAF = mainContainer.createNewAgent(
                    "AgentVolAirFrance",
                    "com.agencevoyage.agents.AgentVol",
                    new Object[0]
            );
            volAF.start();

            AgentController volRy = mainContainer.createNewAgent(
                    "AgentVolRyanair",
                    "com.agencevoyage.agents.AgentVol",
                    new Object[0]
            );
            volRy.start();
            Thread.sleep(500);

            // Create Hotel Agents
            System.out.println("Creating Hotel agents...");
            AgentController hotelLux = mainContainer.createNewAgent(
                    "AgentHotelLuxury",
                    "com.agencevoyage.agents.AgentHotel",
                    new Object[0]
            );
            hotelLux.start();

            AgentController hotelBud = mainContainer.createNewAgent(
                    "AgentHotelBudget",
                    "com.agencevoyage.agents.AgentHotel",
                    new Object[0]
            );
            hotelBud.start();
            Thread.sleep(1000);

            // Create Interface (User Interface)
            System.out.println("\nCreating User Interface...");
            AgentController interfaceAgent = mainContainer.createNewAgent(
                    "AgentInterface",
                    "com.agencevoyage.agents.AgentInterface",
                    new Object[0]
            );
            interfaceAgent.start();

            System.out.println("\n✅ All agents started successfully!");
            System.out.println("📊 Check JADE GUI to see all agents");
            System.out.println("💬 Follow console for user interaction\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}