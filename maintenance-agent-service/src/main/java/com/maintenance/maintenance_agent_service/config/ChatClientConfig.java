package com.maintenance.maintenance_agent_service.config;

import java.util.Map;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
@Configuration
public class ChatClientConfig {

    private static final String SYSTEM_PROMPT = """
            Tu es un assistant de maintenance industrielle. Tu aides les techniciens
            à diagnostiquer des pannes et à suivre les bonnes procédures.

            Règles strictes :
            - Base-toi uniquement sur le contexte documentaire fourni et sur les données
              renvoyées par les outils disponibles (base de données de maintenance).
            - Si une information n'est pas disponible dans le contexte ou via les outils,
              dis clairement que tu ne disposes pas de cette information. N'invente JAMAIS
              une valeur numérique, un seuil de sécurité ou une procédure.
            - Pour toute question portant sur des données chiffrées (seuils, codes erreur,
              fréquences), utilise les outils à ta disposition plutôt que de répondre
              de mémoire.
            - Avant toute requête sur la base, ne suppose jamais le nom d'une table ou d'une colonne, tilise les outils de découverte du schéma pour les connaître.
            - Si un outil renvoie une erreur ou 0 ligne, analyse pourquoi et corrige ta requête
              avant de conclure que l'information n'existe pas.
            """;

    private static final String QA_PROMPT_TEMPLATE = """
            Contexte documentaire (peut être vide ou non pertinent pour la question) :
            ---------------------
            {question_answer_context}
            ---------------------

            Réponds à la question ci-dessous. Si le contexte documentaire ci-dessus ne
            contient PAS l'information demandée (par exemple une valeur chiffrée, un seuil,
            un code erreur), N'INVENTE RIEN : utilise d'abord les outils disponibles pour
            interroger la base de données de maintenance avant de répondre. Ne dis "je ne
            sais pas" qu'après avoir essayé les outils pertinents.

            Question : {query}
            """;

    @Bean
public OpenAiChatModel maintenanceChatModel(
        @Value("${spring.ai.openai.api-key}") String apiKey,
        @Value("${spring.ai.openai.base-url}") String baseUrl,
        @Value("${spring.ai.openai.chat.options.model}") String model,
        ObjectProvider<ObservationRegistry> observationRegistry) {

    OpenAiChatOptions options = OpenAiChatOptions.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .model(model)
            .temperature(0.1)
            .reasoningEffort("medium")
            .maxCompletionTokens(2000)
            .maxRetries(3)
            .extraBody(Map.of("include_reasoning", false))
            .build();

    return OpenAiChatModel.builder()
            .options(options)
            .observationRegistry(observationRegistry.getIfAvailable(() -> ObservationRegistry.NOOP))
            .build();
}

    @Bean
    public ChatClient chatClient(OpenAiChatModel maintenanceChatModel,
                                 VectorStore vectorStore,
                                 ToolCallbackProvider mcpTools) {

        QuestionAnswerAdvisor ragAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(3).build())
                .promptTemplate(new PromptTemplate(QA_PROMPT_TEMPLATE))
                .build();

        var callbacks = mcpTools.getToolCallbacks();
        System.out.println("Tools MCP chargés : " + callbacks.length);
        for (var cb : callbacks) {
            System.out.println(" - " + cb.getToolDefinition().name());
        }

        return ChatClient.builder(maintenanceChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(ragAdvisor)
                .defaultToolCallbacks(callbacks)
                .build();
    }
}