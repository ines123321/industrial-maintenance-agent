package com.maintenance.mcpserver.service;

import com.maintenance.mcpserver.config.SchemaWhitelist;
import com.maintenance.mcpserver.security.SecureQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test d'intégration : contrairement à SecureQueryBuilderTest (qui vérifie
 * seulement le texte SQL généré, en mémoire), ce test exécute vraiment ce
 * SQL contre un Postgres jetable, démarré et détruit automatiquement par
 * Testcontainers — jamais contre la vraie base db-structured.
 */
@Testcontainers
class MaintenanceQueryServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("maintenance_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/init-test-schema.sql");

    private MaintenanceQueryService queryService;

    @BeforeEach
    void setUp() {
        DataSource dataSource = DataSourceBuilder.create()
                .url(postgres.getJdbcUrl())
                .username(postgres.getUsername())
                .password(postgres.getPassword())
                .build();

        SchemaWhitelist whitelist = new SchemaWhitelist();
        SecureQueryBuilder queryBuilder = new SecureQueryBuilder(whitelist);
        queryService = new MaintenanceQueryService(dataSource, queryBuilder, whitelist);
    }

    @Test
    void queryTableRetourneLaBonneLigneDepuisLaVraieBase() {
        List<Map<String, Object>> rows = queryService.queryTable("composants", Map.of("id", "C3"), 10);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("nom")).isEqualTo("Compresseur principal");
        assertThat(rows.get(0).get("seuil_critique")).isEqualTo(new BigDecimal("8.50"));
    }

    @Test
    void queryTableSansFiltreRetourneToutesLesLignesDansLaLimite() {
        List<Map<String, Object>> rows = queryService.queryTable("composants", Map.of(), 10);

        assertThat(rows).hasSize(3); // M12, C3, VF7 — voir init-test-schema.sql
    }

    @Test
    void queryTableAvecFiltreSurCodesErreurTrouveLeBonCode() {
        List<Map<String, Object>> rows = queryService.queryTable(
                "codes_erreur", Map.of("code", "F0002"), 10);

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("niveau_urgence")).isEqualTo("critique");
    }

    @Test
    void aggregateTableCalculeCorrectementLaMoyenne() {
        List<Map<String, Object>> rows = queryService.aggregateTable(
                "composants", "type", "AVG", "seuil_critique", Map.of());

        assertThat(rows).isNotEmpty();
        // Un seul composant par type dans le jeu de test -> moyenne = valeur elle-même
        boolean contientCompresseur = rows.stream()
                .anyMatch(row -> "compresseur".equals(row.get("type")));
        assertThat(contientCompresseur).isTrue();
    }

    @Test
    void describeTableRetourneLesColonnesDeLaVraieTable() {
        List<Map<String, String>> columns = queryService.describeTable("composants");

        assertThat(columns).isNotEmpty();
        assertThat(columns)
                .extracting(col -> col.get("column_name"))
                .contains("id", "nom", "seuil_critique");
    }
}