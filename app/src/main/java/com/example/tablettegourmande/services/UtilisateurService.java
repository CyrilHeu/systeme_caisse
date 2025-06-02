package com.example.tablettegourmande.services;

import com.example.tablettegourmande.models.Utilisateur;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class UtilisateurService {

    public interface OnUtilisateurChangeListener {
        void onUtilisateursChange(List<Utilisateur> utilisateurs);
    }

    private ListenerRegistration listener;

    public UtilisateurService(String restaurantId, OnUtilisateurChangeListener listener) {
        FirebaseFirestore.getInstance()
                .collection("restaurants")
                .document(restaurantId)
                .collection("users")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    List<Utilisateur> liste = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        Utilisateur u = doc.toObject(Utilisateur.class);
                        u.setId(doc.getId());
                        u.setMdp(doc.getString("mot de passe"));
                        liste.add(u);
                    }
                    listener.onUtilisateursChange(liste);
                });
    }
}
