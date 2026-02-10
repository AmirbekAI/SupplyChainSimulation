package domain.model;

import java.util.UUID;

/**
 * Represents a customer in the supply chain system.
 */
public class Customer {
    private final UUID id;
    private String name;
    private Location location;
    private String email;

    public Customer(String name, Location location, String email) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location;
        this.email = email;
    }

    // Constructor for loading existing customers (e.g., from repository)
    public Customer(UUID id, String name, Location location, String email) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.email = email;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public String getEmail() {
        return email;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
