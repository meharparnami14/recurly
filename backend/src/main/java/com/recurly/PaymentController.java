package com.recurly;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository repository;

    public PaymentController(PaymentRepository repository) {
        this.repository = repository;
    }

    // Confirm Payment
    @PostMapping
    public Payment addPayment(@RequestBody Payment payment) {
        payment = new Payment(
                payment.getSubscriptionName(),
                payment.getAmount(),
                LocalDate.now().toString()
        );
        return repository.save(payment);
    }

    // History
    @GetMapping
    public List<Payment> getAllPayments() {
        return repository.findAll();
    }

    // Tracking (pie chart data)
    @GetMapping("/spending")
    public Map<String, Double> getSpendingBySubscription() {
        return repository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        Payment::getSubscriptionName,
                        Collectors.summingDouble(Payment::getAmount)
                ));
    }
}
