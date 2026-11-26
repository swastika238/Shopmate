package com.shopmate.chatbot.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CategorySummary(Long id, String name, String slug) {
}
