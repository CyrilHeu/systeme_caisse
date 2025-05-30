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


    public Produit(String nom, String couleur, double prix, String categorie, List<String> options) {
        this.nom = nom;
        this.couleur = couleur;
        this.prix = prix;
        this.categorie = categorie;
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
    }

    public Produit() {
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
