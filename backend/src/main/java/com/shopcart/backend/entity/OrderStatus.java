package com.shopcart.backend.entity;

public enum OrderStatus {
    PENDING,    // Placed, awaiting payment,
    CONFIRMED,  // Payment successful, ready for processing
    PROCESSING, // Warehouse preparing shipment
    SHIPPED, // In transit with courier
    DELIVERED, // Successfully delivered to customer
    CANCELLED; // Cancelled by user/admin (triggers stock rollback

    public boolean canTransitionTo(OrderStatus next){
        if(next == null){
            return false;
        }

        return switch (this){
            case PENDING -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == PROCESSING || next == CANCELLED;
            case PROCESSING -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}
