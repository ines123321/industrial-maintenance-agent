package com.maintenance.maintenance_agent_service.web;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping
    public ChatResponse ask(@RequestBody ChatRequest request) {
        String answer = chatClient.prompt()
                .user(request.question())
                .call()
                .content();
        return new ChatResponse(answer);
    }

    public record ChatRequest(String question) {}
    public record ChatResponse(String answer) {}
}