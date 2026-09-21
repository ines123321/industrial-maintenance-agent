package com.maintenance.mcpserver.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Source unique de vérité pour tout ce que le MCP server a le droit
 * d'interroger. Rien ici n'est dérivé de l'entrée utilisateur/LLM :
 * ces listes sont codées en dur, c'est ce qui empêche un prompt
 * injection de faire sortir l'agent du périmètre autorisé.
 */
@Component
public class SchemaWhitelist {

    /** Tables accessibles au LLM via les tools. Rien d'autre n'existe pour lui. */
    private static final Set<String> ALLOWED_TABLES = Set.of(
            "composants",
            "codes_erreur",
            "historique_interventions"
    );

    /** Colonnes autorisées, par table (filtres, group by, colonnes agrégées). */
    private static final Map<String, Set<String>> ALLOWED_COLUMNS = Map.of(
            "composants", Set.of(
                    "id", "nom", "type", "seuil_critique", "unite",
                    "frequence_maintenance_h", "localisation"),
            "codes_erreur", Set.of(
                    "code", "composant_id", "cause", "action_corrective", "niveau_urgence"),
            "historique_interventions", Set.of(
                    "id", "composant_id", "date_intervention", "type_intervention",
                    "technicien", "notes")
    );

    /** Fonctions d'agrégation SQL autorisées — jamais une fonction hors de cette liste. */
    private static final Set<String> ALLOWED_AGG_FUNCTIONS = Set.of(
            "COUNT", "AVG", "MAX", "MIN", "SUM"
    );

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    public Set<String> allowedTables() {
        return ALLOWED_TABLES;
    }

    public boolean isTableAllowed(String table) {
        return table != null && ALLOWED_TABLES.contains(table);
    }

    public boolean isColumnAllowed(String table, String column) {
        Set<String> columns = ALLOWED_COLUMNS.get(table);
        return columns != null && columns.contains(column);
    }

    public Set<String> columnsFor(String table) {
        return ALLOWED_COLUMNS.getOrDefault(table, Set.of());
    }

    public boolean isAggFunctionAllowed(String function) {
        return function != null && ALLOWED_AGG_FUNCTIONS.contains(function.toUpperCase());
    }

    public int safeLimit(Integer requested) {
        if (requested == null || requested <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(requested, MAX_LIMIT);
    }
}