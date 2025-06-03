package com.example.tablettegourmande.models;

import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class Produit {
    private String nom;
    private String couleur;
    private double prix;
    private String categorie;

    private List<String> options;

    private String id;

    private Long btn_order;

    private String couleurHex;

    private String etat, commentaire, timestamp_ticket;


    public Produit(String nom, String couleur, double prix, String categorie, long btn_order, List<String> options) {
        this.nom = nom;
        this.couleur = couleur;
        this.prix = prix;
        this.categorie = categorie;
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
        this.btn_order = btn_order;
    }

    public Long getBtn_order() {
        return btn_order != null ? btn_order : 0;
    }

    public void setBtn_order(Long btn_order) {
        this.btn_order = btn_order;
    }

    public Produit() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getTimestamp_ticket() {
        return timestamp_ticket;
    }

    public void setTimestamp_ticket(String timestamp_ticket) {
        this.timestamp_ticket = timestamp_ticket;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    @PropertyName("nom")
    public String getNom() {
        return nom;
    }

    @PropertyName("nom")
    public void setNom(String nom) {
        this.nom = nom;
    }


    public String getCouleur() {
        return couleur;
    }

    public void setCouleur(String couleur) {
        this.couleur = couleur;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

}
