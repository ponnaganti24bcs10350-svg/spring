package com.example.ecommerce.dto;

public class PaymentRequest {
    private String orderId;
    private Double amount;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}
