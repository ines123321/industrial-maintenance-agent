package com.maintenance.maintenance_agent_service.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Ingestion du corpus documentaire (procédures, consignes de sécurité) dans
 * PGVector au démarrage de l'application. Idempotent : si des documents sont
 * déjà présents dans le vector store, l'ingestion est sautée pour éviter les
 * doublons à chaque redémarrage.
 */
@Component
public class DocumentIngestionService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);
    private static final String DOCUMENTS_LOCATION = "classpath:documents/*.md";

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    public DocumentIngestionService(VectorStore vectorStore, DataSource dataSource) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void run(String... args) throws Exception {
        if (alreadyIngested()) {
            log.info("Corpus déjà présent dans le vector store, ingestion sautée.");
            return;
        }

        List<Document> allChunks = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(DOCUMENTS_LOCATION);

        TokenTextSplitter splitter = new TokenTextSplitter();

        for (Resource resource : resources) {
            TextReader reader = new TextReader(resource);
            reader.getCustomMetadata().put("source", resource.getFilename());

            List<Document> documents = reader.read();
            List<Document> chunks = splitter.apply(documents);
            allChunks.addAll(chunks);

            log.info("Document ingéré : {} ({} chunks)", resource.getFilename(), chunks.size());
        }

        vectorStore.add(allChunks);
        log.info("Ingestion terminée : {} chunks au total, {} documents sources.",
                allChunks.size(), resources.length);
    }

    private boolean alreadyIngested() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM vector_store", Integer.class);
            return count != null && count > 0;
        } catch (Exception e) {
            // La table n'existe pas encore (premier démarrage) -> pas encore ingéré
            return false;
        }
    }
}