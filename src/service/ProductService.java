package service;

import domain.model.Product;
import domain.model.ProductType;
import exceptions.ProductNotFoundException;
import repository.InMemoryRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ProductService {
    private InMemoryRepository<Product> storage;

    public ProductService (InMemoryRepository<Product> storage) {
        this.storage = storage;
    }

    public Product createNewProduct(String name, ProductType type, String description) {
        Objects.requireNonNull(name, "Product name cannot be null");
        Objects.requireNonNull(type, "Product type cannot be null");

        Product product = new Product(name, type, description);
        storage.save(product.getId(), product);
        return product;
    }

    public Product getProductById(UUID id) {
        return storage.findById(id).orElseThrow(
                () -> new ProductNotFoundException("Product " + id.toString() + "not found"));
    }

    public Product getProductByName(String name) {
        return storage.findAll().stream()
                .filter(pr -> pr.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException("Product " + name + " not found"));
    }

    public List<Product> getProductsByType(ProductType type) {
        return storage.findAll().stream()
                .filter(pr -> pr.getType() == type)
                .toList();
    }

    public void updateProduct(UUID id, String name, ProductType type, String description) {
        Product product = getProductById(id);
        product.setName(name);
        product.setType(type);
        product.setDescription(description);
        storage.save(id, product);
    }

    public void deleteProduct(UUID id) {
        storage.deleteById(id);
    }

    public List<Product> getAllProducts() {
        return storage.findAll();
    }

    public void deleteAllProductsByType(ProductType type) {
        storage.findAll().stream()
                .filter(pr -> pr.getType() == type)
                .forEach(pr -> storage.deleteById(pr.getId()));
    }
}
