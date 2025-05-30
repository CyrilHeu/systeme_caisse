package com.example.tablettegourmande.permissions;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PermissionManager {

    // Catégories de permissions
    public static final String ACCES_PRINCIPAL = "Accès aux Fonctions Principales";
    public static final String GESTION_PRODUITS = "Gestion des Produits et Stocks";
    public static final String COMMANDES_PAIEMENTS = "Gestion des Commandes et Paiements";
    public static final String HISTORIQUE = "Historiques des Tickets et Rapports";
    public static final String SYSTEME = "Rapports Système";
    public static final String SENSIBLES = "Autres fonctions";

    // Permissions disponibles
    public static final Map<String, List<String>> PERMISSIONS = new LinkedHashMap<>();

    static {
        PERMISSIONS.put(ACCES_PRINCIPAL, Arrays.asList(
                "Accès Restaurant",
                "Gestion des utilisateurs",
                "Personnalisation de l'interface",
                "Mise à jour système",
                "Configuration matériel",
                "Paramétrage du restaurant",
                "Ouverture/Clôture de session de caisse"
        ));

        PERMISSIONS.put(GESTION_PRODUITS, Arrays.asList(
                "Gestion des produits/options/catégories/menus",
                "Modification de stock",
                "Signalement produit manquant",
                "Consultation des stocks"
        ));

        PERMISSIONS.put(COMMANDES_PAIEMENTS, Arrays.asList(
                "Annulation de commande",
                "Modification après validation",
                "Correction d’un paiement",
                "Appliquer une remise (%)",
                "Offrir un produit"
        ));

        PERMISSIONS.put(HISTORIQUE, Arrays.asList(
                "Accès aux rapports et statistiques (généraux)",
                "Accès aux rapports personnels",
                "Historique des commandes (général)",
                "Historique des commandes (personnelles)",
                "Édition de duplicata de ticket",
                "Effectuer un remboursement",
                "Validation requise pour un remboursement"
        ));

        PERMISSIONS.put(SYSTEME, Arrays.asList(
                "Consultation historique des connexions",
                "Consultation des logs système",
                "Rapports de bug"
        ));

        PERMISSIONS.put(SENSIBLES, Arrays.asList(
                "Déconnexion avec tables ouvertes",
                "Réouverture d’un ticket clôturé",
                "Transfert de table (poste ou utilisateur)",
                "Suppression de produit / catégorie / menu"
        ));
    }
}
