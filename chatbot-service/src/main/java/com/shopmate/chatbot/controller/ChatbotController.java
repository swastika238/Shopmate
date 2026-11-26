package com.shopmate.chatbot.controller;

import com.shopmate.chatbot.dto.ChatRequest;
import com.shopmate.chatbot.dto.ChatResponse;
import com.shopmate.chatbot.service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public ChatResponse ask(@RequestHeader(value = "X-User-Id", required = false) Long userId,
                            @Valid @RequestBody ChatRequest request) {
        return chatbotService.ask(userId, request);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "chatbot-service");
    }
}
