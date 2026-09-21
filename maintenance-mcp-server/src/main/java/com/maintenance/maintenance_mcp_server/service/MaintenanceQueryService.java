package com.maintenance.mcpserver.service;

import com.maintenance.mcpserver.config.SchemaWhitelist;
import com.maintenance.mcpserver.security.SecureQueryBuilder;
import com.maintenance.mcpserver.security.SecureQueryBuilder.PreparedQuery;
import com.maintenance.mcpserver.security.SchemaViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MaintenanceQueryService {

    private final JdbcTemplate jdbcTemplate;
    private final SecureQueryBuilder queryBuilder;
    private final SchemaWhitelist whitelist;

    public MaintenanceQueryService(DataSource dataSource,
                                    SecureQueryBuilder queryBuilder,
                                    SchemaWhitelist whitelist) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.queryBuilder = queryBuilder;
        this.whitelist = whitelist;
    }

    public List<String> listAllowedTables() {
        return whitelist.allowedTables().stream().sorted().toList();
    }

    /** Décrit une table à partir de la whitelist (pas de requête sur information_schema en direct). */
    public List<Map<String, String>> describeTable(String table) {
        if (!whitelist.isTableAllowed(table)) {
            throw new SchemaViolationException(
                    "Table non autorisée : '" + table + "'. Tables disponibles : " + whitelist.allowedTables());
        }
        // On interroge PostgreSQL uniquement sur la table déjà validée par la whitelist.
        String sql = """
                SELECT column_name, data_type
                FROM information_schema.columns
                WHERE table_name = ?
                ORDER BY ordinal_position
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, String> column = new LinkedHashMap<>();
            column.put("column_name", rs.getString("column_name"));
            column.put("data_type", rs.getString("data_type"));
            return column;
        }, table);
    }

    public List<Map<String, Object>> queryTable(String table, Map<String, String> filters, Integer limit) {
        PreparedQuery query = queryBuilder.buildSelect(table, filters, limit);
        return executeToMaps(query);
    }

    public List<Map<String, Object>> aggregateTable(String table, String groupByColumn,
                                                      String aggFunction, String aggColumn,
                                                      Map<String, String> filters) {
        PreparedQuery query = queryBuilder.buildAggregate(table, groupByColumn, aggFunction, aggColumn, filters);
        return executeToMaps(query);
    }

    private List<Map<String, Object>> executeToMaps(PreparedQuery query) {
        return jdbcTemplate.query(query.sql(), (rs, rowNum) -> {
            ResultSetMetaData meta = rs.getMetaData();
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            return row;
        }, query.args().toArray());
    }
}