package com.maintenance.maintenance_agent_service.web;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatControllerTest {

    @Test
    void ask_retourneLaReponseDuChatClient() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);

        when(chatClient.prompt()
                .user("Quel est le seuil du moteur M12 ?")
                .call()
                .content())
                .thenReturn("Le seuil est de 80°C.");

        ChatController controller = new ChatController(chatClient);
        ChatController.ChatResponse response =
                controller.ask(new ChatController.ChatRequest("Quel est le seuil du moteur M12 ?"));

        assertEquals("Le seuil est de 80°C.", response.answer());
    }
}