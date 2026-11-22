package com.smartshop.service;

import com.smartshop.dto.PaymentResult;
import com.smartshop.entity.Order;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Slf4j
@Service
public class PaymentService {

    private final String apiKey;
    private final String currency;
    private final RestClient restClient;

    public PaymentService(@Value("${stripe.api.key:}") String apiKey,
                          @Value("${stripe.currency:usd}") String currency) {
        this.apiKey = apiKey;
        this.currency = currency;
        this.restClient = RestClient.builder().baseUrl("https://api.stripe.com").build();
    }

    public PaymentResult process(Order order) {
        if (apiKey == null || apiKey.isBlank()) {
            String transactionId = "MOCK-" + order.getOrderNumber();
            log.info("Stripe is not configured; recording mock payment {}", transactionId);
            return new PaymentResult(true, transactionId,
                    "Mock payment recorded. Configure stripe.api.key for live payments.");
        }
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            long amount = order.getTotalAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValue();
            form.add("amount", Long.toString(amount));
            form.add("currency", currency);
            form.add("description", "SmartShop order " + order.getOrderNumber());
            form.add("automatic_payment_methods[enabled]", "true");

            JsonNode response = restClient.post()
                    .uri("/v1/payment_intents")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.hasNonNull("id")) {
                return new PaymentResult(true, response.get("id").asText(),
                        "Stripe payment intent created");
            }
            return new PaymentResult(false, null, "Stripe returned an unexpected response");
        } catch (Exception ex) {
            log.error("Stripe payment failed for order {}", order.getOrderNumber(), ex);
            return new PaymentResult(false, null, "Payment provider error: " + ex.getMessage());
        }
    }
}
