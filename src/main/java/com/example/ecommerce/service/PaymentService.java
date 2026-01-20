package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentWebhookRequest;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository, RestTemplate restTemplate) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    public Payment initiatePayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setStatus("PENDING");
        payment.setPaymentId("pay_mock_" + UUID.randomUUID().toString());
        payment.setCreatedAt(Instant.now());
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // Trigger mock payment process asynchronously
        mockPaymentProcess(savedPayment);
        
        return savedPayment;
    }

    @Async
    public void mockPaymentProcess(Payment payment) {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(3000); // 3 seconds delay
                
                // Call Webhook
                String webhookUrl = "http://localhost:8080/api/webhooks/payment";
                PaymentWebhookRequest webhookRequest = new PaymentWebhookRequest();
                webhookRequest.setOrderId(payment.getOrderId());
                webhookRequest.setStatus("SUCCESS");
                webhookRequest.setPaymentId(payment.getPaymentId());
                
                restTemplate.postForObject(webhookUrl, webhookRequest, String.class);
                log.info("Webhook triggered for order: " + payment.getOrderId());
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Payment processing interrupted", e);
            } catch (Exception e) {
                log.error("Error triggering webhook", e);
            }
        });
    }

    public void processWebhook(PaymentWebhookRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Payment not found for order: " + request.getOrderId()));
        
        if ("SUCCESS".equals(request.getStatus())) {
            payment.setStatus("SUCCESS");
            paymentRepository.save(payment);
            
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            order.setStatus("PAID");
            orderRepository.save(order);
            
            log.info("Order " + order.getId() + " updated to PAID");
        } else {
            payment.setStatus("FAILED");
            paymentRepository.save(payment);
            log.warn("Payment failed for order: " + request.getOrderId());
        }
    }
}
