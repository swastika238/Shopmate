package com.shopmate.chatbot.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryIntentParserTest {

    private final QueryIntentParser parser = new QueryIntentParser();

    @Test
    void parse_phoneWithMaxPriceAndFeature_extractsFilters() {
        QueryIntentParser.ParsedQuery q = parser.parse("I need a phone under 500 with good battery life");

        assertEquals(QueryIntentParser.Intent.PRODUCT_SEARCH, q.intent());
        assertEquals("smartphones", q.searchRequest().category());
        assertEquals(new BigDecimal("500"), q.searchRequest().maxPrice());
        assertEquals(List.of("battery life"), q.searchRequest().features());
    }

    @Test
    void parse_orderTracking_extractsOrderIntentAndId() {
        QueryIntentParser.ParsedQuery q = parser.parse("Where is my order #42?");

        assertEquals(QueryIntentParser.Intent.ORDER_STATUS, q.intent());
        assertEquals(42L, q.orderId());
    }

    @Test
    void parse_greeting_returnsGreetingIntent() {
        QueryIntentParser.ParsedQuery q = parser.parse("hello");
        assertEquals(QueryIntentParser.Intent.GREETING_OR_HELP, q.intent());
    }
}
