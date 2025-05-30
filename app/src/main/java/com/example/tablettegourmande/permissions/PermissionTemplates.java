package com.example.tablettegourmande.permissions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionTemplates {

    public static Map<String, Boolean> getSuperviseurPermissions() {
        Map<String, Boolean> perms = new HashMap<>();
        for (List<String> droits : PermissionManager.PERMISSIONS.values()) {
            for (String droit : droits) {
                perms.put(droit, true);
            }
        }
        return perms;
    }

    public static Map<String, Boolean> getManagerPermissions() {
        Map<String, Boolean> perms = getSuperviseurPermissions();
        perms.put("Mise à jour système", false);
        perms.put("Configuration matériel", false);
        perms.put("Paramétrage du restaurant", false);
        perms.put("Consultation historique des connexions", false);
        perms.put("Consultation des logs système", false);
        perms.put("Rapports de bug", false);
        perms.put("Gestion des utilisateurs", true);
        return perms;
    }

    public static Map<String, Boolean> getServeurPermissions() {
        Map<String, Boolean> perms = new HashMap<>();
        perms.put("Accès Restaurant", true);
        perms.put("Personnalisation de l'interface", true);
        perms.put("Signalement produit manquant", true);
        perms.put("Accès aux rapports personnels", true);
        perms.put("Historique des commandes (personnelles)", true);
        return perms;
    }
}
