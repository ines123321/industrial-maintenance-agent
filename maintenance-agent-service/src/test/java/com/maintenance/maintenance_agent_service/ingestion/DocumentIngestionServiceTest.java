package com.maintenance.maintenance_agent_service.ingestion;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocumentIngestionServiceTest {

    @Test
    void run_sauteIngestion_siDocumentsDejaPresents() throws Exception {
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        // La base indique qu'il y a déjà 42 documents dans vector_store
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(42);

        DocumentIngestionService service = new DocumentIngestionService(vectorStore, jdbcTemplate);
        service.run();

        // On ne doit JAMAIS réingérer si des documents existent déjà
        verify(vectorStore, never()).add(any());
    }

    @Test
    void run_lanceIngestion_siTableVideOuAbsente() throws Exception {
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        // Cas : la table existe mais est vide
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);

        DocumentIngestionService service = new DocumentIngestionService(vectorStore, jdbcTemplate);
        service.run();

        // L'ingestion doit se déclencher : vectorStore.add() appelé au moins une fois
        verify(vectorStore, atLeastOnce()).add(any());
    }

    @Test
    void run_lanceIngestion_siTableAbsente() throws Exception {
        VectorStore vectorStore = mock(VectorStore.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        // Cas : la table n'existe pas encore -> exception au lieu d'un résultat
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new RuntimeException("relation \"vector_store\" does not exist"));

        DocumentIngestionService service = new DocumentIngestionService(vectorStore, jdbcTemplate);
        service.run();

        verify(vectorStore, atLeastOnce()).add(any());
    }
}