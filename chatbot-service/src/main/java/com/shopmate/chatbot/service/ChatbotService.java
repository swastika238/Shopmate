package com.shopmate.chatbot.service;

import com.shopmate.chatbot.client.*;
import com.shopmate.chatbot.dto.ChatRequest;
import com.shopmate.chatbot.dto.ChatResponse;
import com.shopmate.chatbot.dto.ProductSearchRequest;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    private final QueryIntentParser intentParser;
    private final ProductClient productClient;
    private final OrderClient orderClient;

    public ChatbotService(QueryIntentParser intentParser,
                          ProductClient productClient,
                          OrderClient orderClient) {
        this.intentParser = intentParser;
        this.productClient = productClient;
        this.orderClient = orderClient;
    }

    public ChatResponse ask(Long userId, ChatRequest request) {
        QueryIntentParser.ParsedQuery parsed = intentParser.parse(request.message());

        return switch (parsed.intent()) {
            case GREETING_OR_HELP -> handleGreeting();
            case ORDER_STATUS -> handleOrderInquiry(userId, parsed.orderId());
            case PRODUCT_SEARCH -> handleProductSearch(parsed.searchRequest());
        };
    }

    private ChatResponse handleGreeting() {
        List<String> categories = List.of();
        try {
            categories = productClient.getCategories().stream()
                    .map(CategorySummary::slug)
                    .toList();
        } catch (Exception ex) {
            log.warn("Could not fetch categories during greeting: {}", ex.getMessage());
        }

        String categoryHint = categories.isEmpty()
                ? ""
                : " Available categories: " + String.join(", ", categories) + ".";

        String reply = "Hi! I'm ShopMate's AI shopping assistant. Ask me things like "
                + "\"I need a phone under 500 with good battery life\", "
                + "\"Show wireless audio under 200\", or \"Check my orders\"." + categoryHint;

        return new ChatResponse(reply, "GREETING_OR_HELP", null, List.of(), List.of());
    }

    private ChatResponse handleOrderInquiry(Long userId, Long orderId) {
        if (userId == null) {
            return new ChatResponse(
                    "Please log in and send your JWT Bearer token (or X-User-Id header) so I can look up your orders.",
                    "ORDER_STATUS",
                    null,
                    List.of(),
                    List.of()
            );
        }

        try {
            if (orderId != null) {
                OrderSummary order = orderClient.getOrder(userId, orderId);
                String reply = "Order #" + order.id() + " is currently " + order.status()
                        + " (Total: $" + order.totalAmount() + ").";
                return new ChatResponse(reply, "ORDER_STATUS", null, List.of(), List.of(order));
            }

            List<OrderSummary> orders = orderClient.getMyOrders(userId);
            if (orders.isEmpty()) {
                return new ChatResponse(
                        "You haven't placed any orders yet.",
                        "ORDER_STATUS", null, List.of(), List.of());
            }

            String summary = orders.stream()
                    .limit(5)
                    .map(o -> "#" + o.id() + " (" + o.status() + ", $" + o.totalAmount() + ")")
                    .collect(Collectors.joining(", "));
            return new ChatResponse(
                    "Here are your recent orders: " + summary + ".",
                    "ORDER_STATUS", null, List.of(), orders);
        } catch (FeignException.NotFound ex) {
            return new ChatResponse(
                    "I couldn't find order #" + orderId + " for your account.",
                    "ORDER_STATUS", null, List.of(), List.of());
        } catch (Exception ex) {
            log.warn("Order lookup failed: {}", ex.getMessage());
            return new ChatResponse(
                    "I couldn't reach the Order Service right now. Please try again in a moment.",
                    "ORDER_STATUS", null, List.of(), List.of());
        }
    }

    private ChatResponse handleProductSearch(ProductSearchRequest filters) {
        try {
            List<ProductSummary> matches = productClient.searchProducts(filters);
            if (matches.isEmpty()) {
                return new ChatResponse(
                        "I couldn't find any products matching your criteria. Try widening your price range or checking all categories.",
                        "PRODUCT_SEARCH", filters, List.of(), List.of());
            }

            String topList = matches.stream()
                    .limit(5)
                    .map(p -> p.name() + " ($" + p.price() + ")")
                    .collect(Collectors.joining(", "));

            String reply = "I found " + matches.size() + " matching product(s): " + topList + ".";
            return new ChatResponse(reply, "PRODUCT_SEARCH", filters, matches, List.of());
        } catch (Exception ex) {
            log.warn("Product search failed: {}", ex.getMessage());
            return new ChatResponse(
                    "Product Service is currently unavailable. Please make sure product-service is running on port 8082.",
                    "PRODUCT_SEARCH", filters, List.of(), List.of());
        }
    }
}
