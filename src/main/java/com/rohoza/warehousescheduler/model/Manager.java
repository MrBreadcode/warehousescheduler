package com.rohoza.warehousescheduler.model;

import jakarta.persistence.Entity;

@Entity
public class Manager extends User {
    public Manager() {
        super();
        setRole("MANAGER");
    }

    public Manager(String username, String password) {
        super(username, password, "MANAGER");
    }
}
