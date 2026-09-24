package com.maintenance.maintenance_agent_service.config;

import java.util.List;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dast")
public class DastStubConfig {

    @Bean
    public ChatModel maintenanceChatModel() {
        return prompt -> new ChatResponse(List.of(
                new Generation(new AssistantMessage(
                        "Réponse simulée (profil DAST — aucun appel LLM réel effectué)."))));
    }
}