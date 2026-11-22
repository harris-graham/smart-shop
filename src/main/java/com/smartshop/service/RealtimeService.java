package com.smartshop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

/**
 * Publishes domain events to the STOMP broker so browsers can react without a
 * page refresh. Broadcasting is best-effort: a missing broker connection must
 * never break the surrounding business transaction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RealtimeService {

    static final String INVENTORY_TOPIC = "/topic/inventory";
    static final String ORDER_TOPIC = "/topic/orders";
    static final String CART_QUEUE = "/queue/cart";

    private final SimpMessagingTemplate messagingTemplate;

    public void stockChanged(String smartid, String smartname, int quantity) {
        send(INVENTORY_TOPIC, Map.of(
                "type", "STOCK_CHANGED",
                "smartid", smartid == null ? "" : smartid,
                "smartname": smartname == null ? "" : smartname,
                "quantity", quantity,
                "at", System.currentTimeMillis()));
    }

    public void orderChanged(Integer orderid, String orderNumber, String status) {
        send(ORDER_TOPIC, Map.of(
                "type", "ORDER_CHANGED",
                "orderid": orderid == null ? "" : orderid.toString(),
                "orderNumber": orderNumber == null ? "" : orderNumber,
                "status": status == null ? "" : status,
                "at", System.currentTimeMillis()));
    }

    public void cartChanged(String username, int itemCount) {
        if (username == null || username.isBlank()) {
            return;
        }
        try {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishCart(username, itemCount);
                }
            });
        } catch (IllegalStateException ex) {
            publishCart(username, itemCount);
        }
    }

    private void publishCart(String username, int itemCount) {
        try {
            messagingTemplate.convertAndSendToUser(username, CART_QUEUE,
                    Map.of("type", "CART_CHANGED", "itemCount", itemCount,
                            "at", System.currentTimeMillis()));
        } catch (RuntimeException ex) {
            log.debug("Unable to publish cart update for {}: {}", username, ex.getMessage());
        }
    }

    private void send(String destination, Map<String, Object> payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(destination, payload);
                }
            });
            return;
        }
        publish(destination, payload);
    }

    private void publish(String destination, Map<String, Object> payload) {
        try {
            messagingTemplate.convertAndSend(destination, payload);
        } catch (RuntimeException ex) {
            log.debug("Unable to publish {} to {}: {}", payload, destination, ex.getMessage());
        }
    }
}