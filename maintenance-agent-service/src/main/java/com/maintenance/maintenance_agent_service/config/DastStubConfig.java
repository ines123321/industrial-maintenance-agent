package com.maintenance.maintenance_agent_service.config;

import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Stub actif uniquement en profil "dast".
 * Remplace l'appel réel à Groq par une réponse statique, pour que le scan
 * OWASP ZAP puisse marteler /api/chat (payloads d'injection) sans consommer
 * le moindre quota Groq. Le RAG (embeddings locaux) et les tools MCP restent
 * intacts et sont réellement exercés par le scan.
 */
@Configuration
@Profile("dast")
public class DastStubConfig {

    @Bean
    public ChatModel maintenanceChatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                AssistantMessage message = new AssistantMessage(
                        "Réponse simulée (profil DAST — aucun appel LLM réel effectué).");
                return new ChatResponse(List.of(new Generation(message)));
            }
        };
    }
}