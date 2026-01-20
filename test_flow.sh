#!/bin/bash

BASE_URL="http://localhost:8080/api"

echo "1. Creating Product..."
PRODUCT_RESPONSE=$(curl -s -X POST $BASE_URL/products \
  -H "Content-Type: application/json" \
  -d '{"name": "Gaming Laptop", "description": "High-end gaming laptop", "price": 50000.0, "stock": 10}')
echo "Product Created: $PRODUCT_RESPONSE"
PRODUCT_ID=$(echo $PRODUCT_RESPONSE | grep -o '"id":"[^"]*' | grep -o '[^"]*$')
echo "Product ID: $PRODUCT_ID"

echo -e "\n2. Adding to Cart..."
CART_RESPONSE=$(curl -s -X POST $BASE_URL/cart/add \
  -H "Content-Type: application/json" \
  -d "{\"userId\": \"user123\", \"productId\": \"$PRODUCT_ID\", \"quantity\": 1}")
echo "Added to Cart: $CART_RESPONSE"

echo -e "\n3. Creating Order..."
ORDER_RESPONSE=$(curl -s -X POST $BASE_URL/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": "user123"}')
echo "Order Created: $ORDER_RESPONSE"
ORDER_ID=$(echo $ORDER_RESPONSE | grep -o '"id":"[^"]*' | head -1 | grep -o '[^"]*$')
echo "Order ID: $ORDER_ID"

echo -e "\n4. Initiating Payment..."
PAYMENT_RESPONSE=$(curl -s -X POST $BASE_URL/payments/create \
  -H "Content-Type: application/json" \
  -d "{\"orderId\": \"$ORDER_ID\", \"amount\": 50000.0}")
echo "Payment Initiated: $PAYMENT_RESPONSE"

echo -e "\nWaiting 5 seconds for mock payment webhook..."
sleep 5

echo -e "\n5. Verifying Order Status..."
FINAL_ORDER=$(curl -s -X GET $BASE_URL/orders/$ORDER_ID)
echo "Final Order Status: $FINAL_ORDER"
