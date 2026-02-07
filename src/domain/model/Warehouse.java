package domain.model;

import java.util.UUID;

/**
 * Represents a warehouse in the supply chain system.
 */
public class Warehouse {
    private final UUID id;
    private String name;
    private Location location;
    private int maxCapacity;

    public Warehouse(String name, Location location, int maxCapacity) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location;
        this.maxCapacity = maxCapacity;
    }

    // Constructor for loading existing warehouses (e.g., from repository)
    public Warehouse(UUID id, String name, Location location, int maxCapacity) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.maxCapacity = maxCapacity;
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

    public int getMaxCapacity() {
        return maxCapacity;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public double distanceTo(Location location) {
        return location.calculateDistanceTo(this.location);
    }

    @Override
    public String toString() {
        return "Warehouse{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", maxCapacity=" + maxCapacity +
                '}';
    }
}
