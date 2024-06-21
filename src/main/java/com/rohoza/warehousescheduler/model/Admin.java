package com.rohoza.warehousescheduler.model;

import jakarta.persistence.Entity;

@Entity
public class Admin extends User {
    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(String username, String password) {
        super(username, password, "ADMIN");
    }
}
