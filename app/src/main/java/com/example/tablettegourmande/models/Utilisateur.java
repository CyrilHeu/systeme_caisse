package com.example.tablettegourmande.models;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

public class Utilisateur {
    private String id;
    private String nom;
    private String prenom;
    private String email;
    private String role;

    private Boolean notifyEmail;

    private String numero;

    private String mdp;

    // Constructeur par défaut
    public Utilisateur() {
    }

    // Constructeur avec paramètres
    public Utilisateur(String id, String nom, String prenom, String email, String role, String numero) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.numero = numero;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    // Getters et setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getNotifyEmail() {
        return notifyEmail;
    }

    public void setNotifyEmail(Boolean notifyEmail) {
        this.notifyEmail = notifyEmail;
    }

    // Méthode pour enregistrer un utilisateur dans Firestore
    public static void saveToFirestore(String restaurantId, Utilisateur user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurants")
                .document(restaurantId)
                .collection("users")
                .document(user.getId())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("User", "Utilisateur enregistré avec succès.");
                })
                .addOnFailureListener(e -> {
                    Log.e("User", "Erreur lors de l'enregistrement de l'utilisateur.", e);
                });
    }
}
