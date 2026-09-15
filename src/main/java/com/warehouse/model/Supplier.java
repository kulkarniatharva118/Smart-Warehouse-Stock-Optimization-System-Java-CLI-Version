package com.warehouse.model;

import java.util.Objects;

/**
 * Supplier entity representing a vendor/supplier in the system.
 * Demonstrates CSE2006 Unit 2: Encapsulation & Domain Modeling.
 */
public class Supplier {
    private Long id;
    private String name;
    private String contact;
    private String email;
    private int leadTimeDays;
    private double reliabilityScore; // 0.0 to 5.0

    public Supplier() {
    }

    public Supplier(Long id, String name, String contact, String email, int leadTimeDays, double reliabilityScore) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.email = email;
        this.leadTimeDays = leadTimeDays;
        this.reliabilityScore = reliabilityScore;
    }

    public Supplier(String name, String contact, String email, int leadTimeDays, double reliabilityScore) {
        this(null, name, contact, email, leadTimeDays, reliabilityScore);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(int leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public double getReliabilityScore() {
        return reliabilityScore;
    }

    public void setReliabilityScore(double reliabilityScore) {
        this.reliabilityScore = reliabilityScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Supplier supplier = (Supplier) o;
        return id != null && id.equals(supplier.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return String.format("Supplier[ID=%d, Name='%s', Contact='%s', Email='%s', LeadTime=%d days, Score=%.1f/5.0]",
                id, name, contact, email, leadTimeDays, reliabilityScore);
    }
}
