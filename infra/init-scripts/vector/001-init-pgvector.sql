-- Active l'extension pgvector, nécessaire pour stocker des embeddings
CREATE EXTENSION IF NOT EXISTS vector;

-- Spring AI créera automatiquement la table vector_store au démarrage de l'agent
-- (via spring.ai.vectorstore.pgvector.initialize-schema=true), donc rien d'autre
-- à faire ici pour le moment.