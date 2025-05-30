package com.example.tablettegourmande.models;

import java.util.List;

public class Settings {
    private String language;
    private String timezone;
    private String ticketMessage;
    private List<String> closedDays; // Jours fermés
    private List<String> acceptedPayments; // Modes de paiement acceptés
    private String siret;  // Nouveau champ pour le SIRET
    private String tvaIntracom;  // Nouveau champ pour la TVA intracommunautaire

    // Getters et setters pour Settings
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTicketMessage() {
        return ticketMessage;
    }

    public void setTicketMessage(String ticketMessage) {
        this.ticketMessage = ticketMessage;
    }

    public List<String> getClosedDays() {
        return closedDays;
    }

    public void setClosedDays(List<String> closedDays) {
        this.closedDays = closedDays;
    }

    public List<String> getAcceptedPayments() {
        return acceptedPayments;
    }

    public void setAcceptedPayments(List<String> acceptedPayments) {
        this.acceptedPayments = acceptedPayments;
    }

    public String getSiret() {
        return siret;
    }

    public void setSiret(String siret) {
        this.siret = siret;
    }

    public String getTvaIntracom() {
        return tvaIntracom;
    }

    public void setTvaIntracom(String tvaIntracom) {
        this.tvaIntracom = tvaIntracom;
    }
}