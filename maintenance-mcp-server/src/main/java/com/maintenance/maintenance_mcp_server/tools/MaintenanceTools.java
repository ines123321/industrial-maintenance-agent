package com.maintenance.mcpserver.tools;

import com.maintenance.mcpserver.security.SchemaViolationException;
import com.maintenance.mcpserver.service.MaintenanceQueryService;
import org.springframework.stereotype.Component;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;

import java.util.List;
import java.util.Map;

/**
 * Tools exposés au LLM via MCP. Volontairement génériques (find/aggregate,
 * comme le pattern du serveur MCP officiel MongoDB) plutôt que des tools
 * métier ultra-spécifiques : la sécurité repose sur la couche
 * SchemaWhitelist/SecureQueryBuilder + le rôle SQL read-only, pas sur
 * la restriction des tools eux-mêmes.
 */
@Component
public class MaintenanceTools {

    private final MaintenanceQueryService queryService;

    public MaintenanceTools(MaintenanceQueryService queryService) {
        this.queryService = queryService;
    }

    @McpTool(name = "list_tables",
             description = "Liste les tables de la base de maintenance accessibles à l'agent")
    public List<String> listTables() {
        System.out.println(">>> list_tables");
        List<String> result = queryService.listAllowedTables();
        System.out.println("<<< list_tables resultat=" + result);
        return result;
    }

    @McpTool(name = "describe_table",
             description = "Décrit les colonnes (id, nom, type) d'une table de la base de maintenance")
    public Object describeTable(
            @McpToolParam(description = "Nom exact de la table, ex: 'composants'") String tableName) {

        System.out.println(">>> describe_table table=" + tableName);

        try {
            Object result = queryService.describeTable(tableName);
            System.out.println("<<< describe_table resultat=" + result);
            return result;
        } catch (SchemaViolationException e) {
            System.out.println("<<< describe_table erreur=" + e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    @McpTool(name = "query_table",
             description = "Interroge une table avec des filtres colonne=valeur exacte. Maximum 50 lignes."
            + "Appelle d'abord describe_table si tu ne connais pas les colonnes.")
    public Object queryTable(
            @McpToolParam(description = "Nom exact de la table") String tableName,
            @McpToolParam(description = "Filtres clé-valeur, colonne=valeur exacte. Peut être vide.")
            Map<String, String> filters,
            @McpToolParam(description = "Nombre max de lignes, 20 par défaut, 50 maximum", required = false)
            Integer limit) {

        System.out.println(">>> query_table table=" + tableName + " filters=" + filters + " limit=" + limit);

        try {
            Object result = queryService.queryTable(tableName, filters, limit);
            System.out.println("<<< query_table resultat=" + result);
            return result;
        } catch (SchemaViolationException e) {
            System.out.println("<<< query_table erreur=" + e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    @McpTool(name = "aggregate_table",
             description = "Agrège une colonne numérique groupée par une autre colonne. Fonctions : COUNT, AVG, MAX, MIN, SUM.")
    public Object aggregateTable(
            @McpToolParam(description = "Nom exact de la table") String tableName,
            @McpToolParam(description = "Colonne de regroupement (GROUP BY)") String groupByColumn,
            @McpToolParam(description = "Fonction : COUNT, AVG, MAX, MIN ou SUM") String aggFunction,
            @McpToolParam(description = "Colonne numérique à agréger") String aggColumn,
            @McpToolParam(description = "Filtres clé-valeur optionnels", required = false)
            Map<String, String> filters) {

        System.out.println(">>> aggregate_table table=" + tableName + " groupBy=" + groupByColumn
                + " fn=" + aggFunction + " col=" + aggColumn + " filters=" + filters);

        try {
            Object result = queryService.aggregateTable(tableName, groupByColumn, aggFunction, aggColumn, filters);
            System.out.println("<<< aggregate_table resultat=" + result);
            return result;
        } catch (SchemaViolationException e) {
            System.out.println("<<< aggregate_table erreur=" + e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }
}