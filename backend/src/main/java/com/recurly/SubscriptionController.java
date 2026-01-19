package com.recurly;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionRepository repository;

    public SubscriptionController(SubscriptionRepository repository) {
        this.repository = repository;
    }

    // Dashboard
    @GetMapping
    public List<Subscription> getAllSubscriptions() {
        return repository.findAll();
    }

    // Add Subscription
    @PostMapping
    public Subscription addSubscription(@RequestBody Subscription subscription) {
        return repository.save(subscription);
    }

    // Delete Subscription
    @DeleteMapping("/{id}")
    public void deleteSubscription(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
