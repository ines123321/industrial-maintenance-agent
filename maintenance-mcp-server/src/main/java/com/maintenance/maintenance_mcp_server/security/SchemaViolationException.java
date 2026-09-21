package com.maintenance.mcpserver.security;

/**
 * Levée dès qu'une table, une colonne ou une fonction demandée par le LLM
 * n'est pas dans la whitelist. Ne jamais laisser passer une requête sur
 * la base de simples suppositions — on échoue de façon explicite.
 */
public class SchemaViolationException extends RuntimeException {

    public SchemaViolationException(String message) {
        super(message);
    }
}