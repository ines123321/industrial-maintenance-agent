package com.maintenance.mcpserver.security;

import com.maintenance.mcpserver.config.SchemaWhitelist;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Construit du SQL sûr à partir d'entrées potentiellement fournies par un LLM.
 *
 * Règle absolue : les noms de table/colonne/fonction ne peuvent JAMAIS être
 * liés via un PreparedStatement (JDBC ne le permet pas), donc ils sont
 * strictement validés contre la {@link SchemaWhitelist} avant d'être
 * insérés dans le texte SQL. Les valeurs, elles, sont toujours passées
 * en paramètres liés (`?`), jamais concaténées.
 */
@Component
public class SecureQueryBuilder {

    private final SchemaWhitelist whitelist;

    public SecureQueryBuilder(SchemaWhitelist whitelist) {
        this.whitelist = whitelist;
    }

    /** Résultat prêt à exécuter : SQL paramétré + valeurs à lier dans l'ordre. */
    public record PreparedQuery(String sql, List<Object> args) {}

    public PreparedQuery buildSelect(String table, Map<String, String> filters, Integer limit) {
        requireTableAllowed(table);

        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(table);
        List<Object> args = new ArrayList<>();

        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            boolean first = true;
            for (Map.Entry<String, String> filter : filters.entrySet()) {
                requireColumnAllowed(table, filter.getKey());
                if (!first) {
                    sql.append(" AND ");
                }
                sql.append(filter.getKey()).append(" = ?");
                args.add(filter.getValue());
                first = false;
            }
        }

        sql.append(" LIMIT ?");
        args.add(whitelist.safeLimit(limit));

        return new PreparedQuery(sql.toString(), args);
    }

    public PreparedQuery buildAggregate(String table, String groupByColumn,
                                         String aggFunction, String aggColumn,
                                         Map<String, String> filters) {
        requireTableAllowed(table);
        requireColumnAllowed(table, groupByColumn);
        requireColumnAllowed(table, aggColumn);
        requireAggFunctionAllowed(aggFunction);

        String normalizedFunction = aggFunction.toUpperCase();
        StringBuilder sql = new StringBuilder("SELECT ")
                .append(groupByColumn).append(", ")
                .append(normalizedFunction).append("(").append(aggColumn).append(") AS result")
                .append(" FROM ").append(table);
        List<Object> args = new ArrayList<>();

        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            boolean first = true;
            for (Map.Entry<String, String> filter : filters.entrySet()) {
                requireColumnAllowed(table, filter.getKey());
                if (!first) {
                    sql.append(" AND ");
                }
                sql.append(filter.getKey()).append(" = ?");
                args.add(filter.getValue());
                first = false;
            }
        }

        sql.append(" GROUP BY ").append(groupByColumn);

        return new PreparedQuery(sql.toString(), args);
    }

    private void requireTableAllowed(String table) {
        if (!whitelist.isTableAllowed(table)) {
            throw new SchemaViolationException(
                    "Table non autorisée : '" + table + "'. Tables disponibles : " + whitelist.allowedTables());
        }
    }

    private void requireColumnAllowed(String table, String column) {
        if (!whitelist.isColumnAllowed(table, column)) {
            throw new SchemaViolationException(
                    "Colonne non autorisée : '" + column + "' sur la table '" + table + "'.");
        }
    }

    private void requireAggFunctionAllowed(String function) {
        if (!whitelist.isAggFunctionAllowed(function)) {
            throw new SchemaViolationException(
                    "Fonction d'agrégation non autorisée : '" + function
                            + "'. Autorisées : COUNT, AVG, MAX, MIN, SUM.");
        }
    }
}