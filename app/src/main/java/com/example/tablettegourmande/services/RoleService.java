package com.example.tablettegourmande.services;

import com.example.tablettegourmande.models.Role;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class RoleService {

    public interface OnRolesChangeListener {
        void onRolesChange(List<Role> roles);
    }

    private ListenerRegistration listener;

    public RoleService(String restaurantId, OnRolesChangeListener listener) {
        FirebaseFirestore.getInstance()
                .collection("restaurants")
                .document(restaurantId)
                .collection("roles")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;

                    List<Role> liste = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        Role role = doc.toObject(Role.class);
                        if (role != null) {
                            role.setId(doc.getId());
                            liste.add(role);
                        }
                    }
                    listener.onRolesChange(liste);
                });
    }
}
