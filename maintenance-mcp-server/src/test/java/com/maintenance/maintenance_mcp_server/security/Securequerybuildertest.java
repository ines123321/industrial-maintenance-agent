package com.maintenance.mcpserver.security;

import com.maintenance.mcpserver.config.SchemaWhitelist;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecureQueryBuilderTest {

    private final SchemaWhitelist whitelist = new SchemaWhitelist();
    private final SecureQueryBuilder queryBuilder = new SecureQueryBuilder(whitelist);

    @Test
    void buildSelectSansFiltreAjouteSeulementLeLimit() {
        SecureQueryBuilder.PreparedQuery query = queryBuilder.buildSelect("composants", Map.of(), 10);

        assertThat(query.sql()).isEqualTo("SELECT * FROM composants LIMIT ?");
        assertThat(query.args()).containsExactly(10);
    }

    @Test
    void buildSelectAvecFiltreAjouteLaClauseWhere() {
        SecureQueryBuilder.PreparedQuery query =
                queryBuilder.buildSelect("composants", Map.of("type", "moteur"), 5);

        assertThat(query.sql()).isEqualTo("SELECT * FROM composants WHERE type = ? LIMIT ?");
        assertThat(query.args()).containsExactly("moteur", 5);
    }

    @Test
    void buildSelectAvecLimitNonSpecifieUtiliseLeDefaut() {
        SecureQueryBuilder.PreparedQuery query = queryBuilder.buildSelect("composants", Map.of(), null);

        assertThat(query.args()).containsExactly(20);
    }

    @Test
    void buildSelectRejetteUneTableHorsWhitelist() {
        assertThatThrownBy(() -> queryBuilder.buildSelect("pg_catalog", Map.of(), 10))
                .isInstanceOf(SchemaViolationException.class)
                .hasMessageContaining("Table non autorisée");
    }

    @Test
    void buildSelectRejetteUneColonneHorsWhitelistDansLeFiltre() {
        assertThatThrownBy(() ->
                queryBuilder.buildSelect("composants", Map.of("colonne_inconnue", "x"), 10))
                .isInstanceOf(SchemaViolationException.class)
                .hasMessageContaining("Colonne non autorisée");
    }

    @Test
    void buildSelectPlafonneLeLimitMemeSiEnormeDemande() {
        SecureQueryBuilder.PreparedQuery query = queryBuilder.buildSelect("composants", Map.of(), 10000);

        assertThat(query.args()).containsExactly(50);
    }

    @Test
    void buildAggregateConstruitLaRequeteGroupBy() {
        SecureQueryBuilder.PreparedQuery query = queryBuilder.buildAggregate(
                "composants", "type", "AVG", "seuil_critique", Map.of());

        assertThat(query.sql()).isEqualTo(
                "SELECT type, AVG(seuil_critique) AS result FROM composants GROUP BY type");
    }

    @Test
    void buildAggregateNormaliseLaCasseDeLaFonction() {
        SecureQueryBuilder.PreparedQuery query = queryBuilder.buildAggregate(
                "composants", "type", "avg", "seuil_critique", Map.of());

        assertThat(query.sql()).contains("AVG(seuil_critique)");
    }

    @Test
    void buildAggregateRejetteUneFonctionHorsWhitelist() {
        assertThatThrownBy(() -> queryBuilder.buildAggregate(
                "composants", "type", "DROP", "seuil_critique", Map.of()))
                .isInstanceOf(SchemaViolationException.class)
                .hasMessageContaining("Fonction d'agrégation non autorisée");
    }

    @Test
    void buildAggregateRejetteUneTableHorsWhitelist() {
        assertThatThrownBy(() -> queryBuilder.buildAggregate(
                "utilisateurs", "role", "COUNT", "id", Map.of()))
                .isInstanceOf(SchemaViolationException.class)
                .hasMessageContaining("Table non autorisée");
    }

    @Test
    void buildAggregateRejetteUneColonneDAgregationHorsWhitelist() {
        assertThatThrownBy(() -> queryBuilder.buildAggregate(
                "composants", "type", "AVG", "colonne_inconnue", Map.of()))
                .isInstanceOf(SchemaViolationException.class)
                .hasMessageContaining("Colonne non autorisée");
    }
}