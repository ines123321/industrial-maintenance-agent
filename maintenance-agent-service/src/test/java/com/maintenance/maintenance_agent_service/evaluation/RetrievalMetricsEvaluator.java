package com.maintenance.maintenance_agent_service.evaluation;


import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

/**
 * Mesure si la recherche vectorielle a bien récupéré le document source
 * attendu, sans consulter le LLM — calcul pur sur les métadonnées des
 * chunks récupérés. Complète RelevancyEvaluator/FactCheckingEvaluator
 * (qui jugent la réponse finale) en jugeant l'étape de récupération
 * elle-même.
 */
public class RetrievalMetricsEvaluator {

    private final VectorStore vectorStore;

    public RetrievalMetricsEvaluator(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public record RetrievalMetrics(double precision, double recall, List<String> retrievedSources) {}

    /**
     * @param question       la question posée
     * @param expectedSource le nom de fichier attendu (ex: "proc-loto-m12.md")
     */
    public RetrievalMetrics evaluate(String question, String expectedSource) {
        SearchRequest request = SearchRequest.builder().query(question).topK(3).build();
        List<Document> results = vectorStore.similaritySearch(request);

        List<String> retrievedSources = results.stream()
                .map(doc -> String.valueOf(doc.getMetadata().getOrDefault("source", "unknown")))
                .toList();

        long relevantRetrieved = retrievedSources.stream()
                .filter(source -> source.equals(expectedSource))
                .count();

        // Précision : parmi les chunks récupérés, combien viennent du bon document
        double precision = retrievedSources.isEmpty()
                ? 0.0
                : (double) relevantRetrieved / retrievedSources.size();

        // Rappel : le bon document a-t-il été récupéré au moins une fois
        // (un seul document pertinent par question dans ce dataset)
        double recall = relevantRetrieved > 0 ? 1.0 : 0.0;

        return new RetrievalMetrics(precision, recall, retrievedSources);
    }
}