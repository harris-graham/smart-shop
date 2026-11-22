package com.smartshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over WebSocket endpoint used for live stock, order and cart updates.
 *
 * <p>The broker prefixes are:
 * <ul>
 *   <li>{@code /topic/inventory} – stock levels changed.</li>
 *   <li>{@code /topic/orders} – an order was created or changed status.</li>
 *   <li>{@code /queue/cart} – per-session cart counters (user destination).</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS also accepts raw WebSocket upgrades, so a single endpoint serves
        // both modern browsers and the STOMP client in static/js/realtime.js.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}