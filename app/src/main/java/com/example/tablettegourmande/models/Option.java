package com.example.tablettegourmande.models;

import java.util.List;

public class Option {
    private String nom;
    private boolean multiple;
    private List<String> choix;

    public Option() {
        // Constructeur par défaut requis pour Firebase
    }

    public Option(String nom, boolean multiple, List<String> choix) {
        this.nom = nom;
        this.multiple = multiple;
        this.choix = choix;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public List<String> getChoix() {
        return choix;
    }

    public void setChoix(List<String> choix) {
        this.choix = choix;
    }
}