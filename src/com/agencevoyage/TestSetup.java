package com.agencevoyage;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.StaleProxyException;

public class TestSetup {
    public static void main(String[] args) {
        try {
            // Obtenir le runtime JADE
            Runtime rt = Runtime.instance();

            // Créer un profil avec GUI
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "true");

            // Créer le conteneur principal
            AgentContainer mainContainer = rt.createMainContainer(profile);

            System.out.println(" JADE est correctement configuré !");
            System.out.println(" L'interface JADE devrait s'ouvrir...");

        } catch (Exception e) {
            System.err.println(" Erreur lors du lancement de JADE:");
            e.printStackTrace();
        }
    }
}