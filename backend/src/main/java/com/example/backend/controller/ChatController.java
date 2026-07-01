package com.example.backend.controller;

import com.example.backend.dto.ChatRequest;
import com.example.backend.dto.ChatResponse;
import com.example.backend.service.ChatSuggestionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatSuggestionService chatSuggestionService;

    public ChatController(ChatSuggestionService chatSuggestionService) {
        this.chatSuggestionService = chatSuggestionService;
    }

    @PostMapping("/api/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return chatSuggestionService.suggest(request.message());
    }
}