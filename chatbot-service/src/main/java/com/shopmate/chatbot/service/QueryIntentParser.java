package com.shopmate.chatbot.service;

import com.shopmate.chatbot.dto.ProductSearchRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class QueryIntentParser {

    public enum Intent {
        GREETING_OR_HELP,
        ORDER_STATUS,
        PRODUCT_SEARCH
    }

    public record ParsedQuery(
            Intent intent,
            ProductSearchRequest searchRequest,
            Long orderId
    ) {}

    private static final Pattern BETWEEN_PATTERN =
            Pattern.compile("between\\s+\\$?(\\d+(?:\\.\\d+)?)\\s+(?:and|to)\\s+\\$?(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MAX_PRICE_PATTERN =
            Pattern.compile("(?:under|below|less than|max|up to|<=)\\s+\\$?(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MIN_PRICE_PATTERN =
            Pattern.compile("(?:above|over|more than|min|at least|>=)\\s+\\$?(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDER_ID_PATTERN =
            Pattern.compile("order\\s*#?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    private static final Map<String, String> CATEGORY_SYNONYMS = Map.ofEntries(
            Map.entry("smartphone", "smartphones"),
            Map.entry("smartphones", "smartphones"),
            Map.entry("phone", "smartphones"),
            Map.entry("phones", "smartphones"),
            Map.entry("mobile", "smartphones"),
            Map.entry("laptop", "laptops"),
            Map.entry("laptops", "laptops"),
            Map.entry("notebook", "laptops"),
            Map.entry("headphone", "audio"),
            Map.entry("headphones", "audio"),
            Map.entry("earbuds", "audio"),
            Map.entry("audio", "audio"),
            Map.entry("speaker", "audio"),
            Map.entry("charger", "accessories"),
            Map.entry("adapter", "accessories"),
            Map.entry("cable", "accessories"),
            Map.entry("accessory", "accessories"),
            Map.entry("accessories", "accessories")
    );

    private static final List<String> KNOWN_FEATURES = List.of(
            "battery life", "fast charging", "5g", "wireless", "bluetooth",
            "noise cancellation", "amoled", "camera", "ssd", "16gb ram",
            "lightweight", "usb-c", "compact"
    );

    private static final Set<String> STOP_WORDS = Set.of(
            "i", "me", "my", "need", "want", "looking", "for", "a", "an", "the",
            "with", "good", "best", "cheap", "show", "find", "search", "recommend",
            "suggest", "under", "below", "above", "over", "between", "and", "to",
            "less", "more", "than", "please", "any", "products", "product", "item", "items"
    );

    public ParsedQuery parse(String rawMessage) {
        String lower = rawMessage.toLowerCase(Locale.ROOT).trim();

        if (isGreetingOrHelp(lower)) {
            return new ParsedQuery(Intent.GREETING_OR_HELP, null, null);
        }

        if (isOrderIntent(lower)) {
            Matcher orderMatcher = ORDER_ID_PATTERN.matcher(lower);
            Long orderId = orderMatcher.find() ? Long.parseLong(orderMatcher.group(1)) : null;
            return new ParsedQuery(Intent.ORDER_STATUS, null, orderId);
        }

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        Matcher between = BETWEEN_PATTERN.matcher(lower);
        if (between.find()) {
            minPrice = new BigDecimal(between.group(1));
            maxPrice = new BigDecimal(between.group(2));
        } else {
            Matcher maxM = MAX_PRICE_PATTERN.matcher(lower);
            if (maxM.find()) {
                maxPrice = new BigDecimal(maxM.group(1));
            }
            Matcher minM = MIN_PRICE_PATTERN.matcher(lower);
            if (minM.find()) {
                minPrice = new BigDecimal(minM.group(1));
            }
        }

        String category = null;
        String[] tokens = lower.replaceAll("[^a-z0-9\\s-]", " ").split("\\s+");
        for (String token : tokens) {
            if (CATEGORY_SYNONYMS.containsKey(token)) {
                category = CATEGORY_SYNONYMS.get(token);
                break;
            }
        }

        List<String> features = new ArrayList<>();
        for (String feature : KNOWN_FEATURES) {
            if (lower.contains(feature)) {
                features.add(feature);
            }
        }

        String keyword = null;
        if (category == null && features.isEmpty() && minPrice == null && maxPrice == null) {
            List<String> meaningful = Arrays.stream(tokens)
                    .filter(t -> !t.isBlank() && !STOP_WORDS.contains(t) && !t.matches("\\d+(?:\\.\\d+)?"))
                    .toList();
            if (!meaningful.isEmpty()) {
                keyword = String.join(" ", meaningful);
            }
        }

        ProductSearchRequest searchRequest = new ProductSearchRequest(
                minPrice,
                maxPrice,
                category,
                features.isEmpty() ? null : features,
                keyword
        );

        return new ParsedQuery(Intent.PRODUCT_SEARCH, searchRequest, null);
    }

    private boolean isGreetingOrHelp(String lower) {
        return lower.matches("^(hi|hello|hey|help|what can you do|start)\\b[!.?\\s]*$");
    }

    private boolean isOrderIntent(String lower) {
        return lower.contains("order") || lower.contains("track") || lower.contains("shipment");
    }
}
