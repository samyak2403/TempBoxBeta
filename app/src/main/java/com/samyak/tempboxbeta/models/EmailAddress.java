package com.samyak.tempboxbeta.models;

public class EmailAddress {
    private String name;
    private String address;

    // Constructors
    public EmailAddress() {}

    public EmailAddress(String name, String address) {
        this.name = name;
        this.address = address;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    @Override
    public String toString() {
        if (name != null && !name.isEmpty()) {
            return name + " <" + address + ">";
        }
        return address;
    }
} 