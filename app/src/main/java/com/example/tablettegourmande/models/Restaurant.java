package com.example.tablettegourmande.models;

import com.google.firebase.firestore.FieldValue;

import java.util.List;

public class Restaurant {
    private String name;
    private LocationInfo locationInfo = new LocationInfo();
    private ContactInfo contactInfo = new ContactInfo();
    private Settings settings = new Settings();  // Initialisation de Settings

    private String ownerId;

    private FieldValue createdAt;

    // Getters et setters pour les autres attributs...

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocationInfo getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(LocationInfo locationInfo) {
        this.locationInfo = locationInfo;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(ContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public FieldValue getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(FieldValue createdAt) {
        this.createdAt = createdAt;
    }

    // Classe interne LocationInfo
    public static class LocationInfo {
        private String address;
        private String city;
        private String postalCode;
        private String country;

        // Getters et setters pour LocationInfo
        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }


    }

    // Classe interne ContactInfo
    public static class ContactInfo {
        private String phone;
        private String email;
        private Boolean notifyEmail;

        // Getters et setters pour ContactInfo
        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Boolean getNotifyEmail() {
            return notifyEmail;
        }

        public void setNotifyEmail(Boolean notifyEmail) {
            this.notifyEmail = notifyEmail;
        }
    }

}
