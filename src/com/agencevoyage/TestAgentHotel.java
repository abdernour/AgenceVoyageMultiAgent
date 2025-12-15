package com.agencevoyage;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class TestAgentHotel {
    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();

            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "true");

            AgentContainer mainContainer = rt.createMainContainer(profile);

            // Create Agent Hotel Luxury
            AgentController hotelLuxury = mainContainer.createNewAgent(
                    "AgentHotelLuxury",
                    "com.agencevoyage.agents.AgentHotel",
                    new Object[0]
            );
            hotelLuxury.start();

            // Create Agent Hotel Budget
            AgentController hotelBudget = mainContainer.createNewAgent(
                    "AgentHotelBudget",
                    "com.agencevoyage.agents.AgentHotel",
                    new Object[0]
            );
            hotelBudget.start();

            System.out.println("✅ Both Agent Hotel instances are running!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}