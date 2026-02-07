package service;

import domain.model.Customer;
import domain.model.Location;
import exceptions.CustomerNotFoundException;
import repository.InMemoryRepository;

import java.util.List;
import java.util.UUID;

public class CustomerService {
    private final InMemoryRepository<Customer> storage;

    public CustomerService(InMemoryRepository<Customer> storage) {
        this.storage = storage;
    }

    public Customer getCustomerById(UUID customerId) {
        return storage.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer: " + customerId + " not found"));
    }

    public Location getCustomerLocation(UUID customerId) {
        Customer customer = getCustomerById(customerId);
        return customer.getLocation();
    }

    public List<Customer> getAllCustomers() {
        return storage.findAll();
    }

    public List<Customer> findAllCustomersInLocation(Location location) {
        return storage.findAll().stream()
                .filter(cs -> cs.getLocation().equals(location))
                .toList();
    }
}
