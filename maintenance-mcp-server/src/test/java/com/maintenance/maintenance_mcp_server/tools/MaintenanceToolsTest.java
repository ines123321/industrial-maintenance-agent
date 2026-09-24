package com.maintenance.mcpserver.tools;

import com.maintenance.mcpserver.security.SchemaViolationException;
import com.maintenance.mcpserver.service.MaintenanceQueryService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MaintenanceToolsTest {

        @Test
    void describeTable_retourneLeResultat_siPasDErreur() {
        MaintenanceQueryService queryService = mock(MaintenanceQueryService.class);
        doReturn(List.of(Map.of("colonne", "id", "type", "integer")))
                .when(queryService).describeTable("composants");

        MaintenanceTools tools = new MaintenanceTools(queryService);
        Object result = tools.describeTable("composants");

        assertEquals(List.of(Map.of("colonne", "id", "type", "integer")), result);
    }

    @Test
    void describeTable_retourneMapErreur_siSchemaViolation() {
        MaintenanceQueryService queryService = mock(MaintenanceQueryService.class);
        doThrow(new SchemaViolationException("Table non autorisée"))
                .when(queryService).describeTable("table_interdite");

        MaintenanceTools tools = new MaintenanceTools(queryService);
        Object result = tools.describeTable("table_interdite");

        assertEquals(Map.of("error", "Table non autorisée"), result);
    }

    @Test
    void queryTable_retourneLeResultat_siPasDErreur() {
        MaintenanceQueryService queryService = mock(MaintenanceQueryService.class);
        Map<String, String> filters = Map.of("statut", "actif");
        doReturn(List.of(Map.of("id", 1, "nom", "Moteur M12")))
                .when(queryService).queryTable("composants", filters, 20);

        MaintenanceTools tools = new MaintenanceTools(queryService);
        Object result = tools.queryTable("composants", filters, 20);

        assertEquals(List.of(Map.of("id", 1, "nom", "Moteur M12")), result);
    }

    @Test
    void queryTable_retourneMapErreur_siSchemaViolation() {
        MaintenanceQueryService queryService = mock(MaintenanceQueryService.class);
        Map<String, String> filters = Map.of("colonne_inexistante", "x");
        doThrow(new SchemaViolationException("Colonne non autorisée"))
                .when(queryService).queryTable("composants", filters, 20);

        MaintenanceTools tools = new MaintenanceTools(queryService);
        Object result = tools.queryTable("composants", filters, 20);

        assertEquals(Map.of("error", "Colonne non autorisée"), result);
    }

    @Test
    void aggregateTable_retourneLeResultat_siPasDErreur() {
        MaintenanceQueryService queryService = mock(MaintenanceQueryService.class);
        doReturn(List.of(Map.of("statut", "actif", "count", 5)))
                .when(queryService).aggregateTable("composants", "statut", "COUNT", "id", null);

        MaintenanceTools tools = new MaintenanceTools(queryService);
        Object result = tools.aggregateTable("composants", "statut", "COUNT", "id", null);

        assertEquals(List.of(Map.of("statut", "actif", "count", 5)), result);
    }

    @Test
    void aggregateTable_retourneMapErreur_siSchemaViolation() {
        MaintenanceQueryService queryService = mock(MaintenanceQueryService.class);
        doThrow(new SchemaViolationException("Table non autorisée"))
                .when(queryService).aggregateTable("table_interdite", "statut", "COUNT", "id", null);

        MaintenanceTools tools = new MaintenanceTools(queryService);
        Object result = tools.aggregateTable("table_interdite", "statut", "COUNT", "id", null);

        assertEquals(Map.of("error", "Table non autorisée"), result);
    }
}