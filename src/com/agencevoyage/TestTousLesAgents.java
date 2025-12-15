package com.agencevoyage;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class TestTousLesAgents {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();

            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "true");

            AgentContainer mainContainer = rt.createMainContainer(profile);

            System.out.println("=== Creating Vol Agents ===");

            // Agents Vol
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

            System.out.println("\n=== Creating Hotel Agents ===");

            // Agents Hotel
            AgentController hotelLux = mainContainer.createNewAgent(
                    "AgentHotelLuxury",
                    "com.agencevoyage.agents.AgentHotel",
                    new Object[0]
            );
            hotelLux.start();

            AgentController hotelBudget = mainContainer.createNewAgent(
                    "AgentHotelBudget",
                    "com.agencevoyage.agents.AgentHotel",
                    new Object[0]
            );
            hotelBudget.start();

            System.out.println("\n✅ All 4 service provider agents are running!");
            System.out.println("📊 Check JADE GUI - you should see 4 green agents");
            System.out.println("🔍 Open DF GUI to see registered services");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}