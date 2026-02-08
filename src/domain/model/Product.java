package domain.model;

import java.util.UUID;

/**
 * Represents a product in the supply chain system.
 */
public class Product {
    private final UUID id;
    private String name;
    private ProductType type;
    private String description;

    public Product(String name, ProductType type, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.type = type;
        this.description = description;
    }

    // Constructor for loading existing products (e.g., from repository)
    public Product(UUID id, String name, ProductType type, String description) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ProductType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
