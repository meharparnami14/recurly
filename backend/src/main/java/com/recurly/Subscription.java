package com.recurly;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String billingCycle;
    private double amount;
    private String nextPaymentDate;

    public Subscription() {
        // required by JPA
    }

    public Subscription(String name, String billingCycle, double amount, String nextPaymentDate) {
        this.name = name;
        this.billingCycle = billingCycle;
        this.amount = amount;
        this.nextPaymentDate = nextPaymentDate;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBillingCycle() {
        return billingCycle;
    }

    public double getAmount() {
        return amount;
    }

    public String getNextPaymentDate() {
        return nextPaymentDate;
    }
}
