package com.maintenance.mcpserver.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaWhitelistTest {

    private final SchemaWhitelist whitelist = new SchemaWhitelist();

    @Test
    void tableAutoriseeEstAcceptee() {
        assertThat(whitelist.isTableAllowed("composants")).isTrue();
        assertThat(whitelist.isTableAllowed("codes_erreur")).isTrue();
        assertThat(whitelist.isTableAllowed("historique_interventions")).isTrue();
    }

    @Test
    void tableNonAutoriseeEstRejetee() {
        assertThat(whitelist.isTableAllowed("information_schema")).isFalse();
        assertThat(whitelist.isTableAllowed("pg_catalog")).isFalse();
        assertThat(whitelist.isTableAllowed("utilisateurs")).isFalse();
    }

    @Test
    void tableNulleEstRejetee() {
        assertThat(whitelist.isTableAllowed(null)).isFalse();
    }

    @Test
    void colonneAutoriseeSurTableCorrecteEstAcceptee() {
        assertThat(whitelist.isColumnAllowed("composants", "seuil_critique")).isTrue();
        assertThat(whitelist.isColumnAllowed("codes_erreur", "niveau_urgence")).isTrue();
    }

    @Test
    void colonneNonAutoriseeEstRejetee() {
        assertThat(whitelist.isColumnAllowed("composants", "colonne_inexistante")).isFalse();
    }

    @Test
    void colonneValideSurMauvaiseTableEstRejetee() {
        assertThat(whitelist.isColumnAllowed("composants", "niveau_urgence")).isFalse();
    }

    @Test
    void fonctionsAgregationAutoriseesSontAcceptees() {
        assertThat(whitelist.isAggFunctionAllowed("COUNT")).isTrue();
        assertThat(whitelist.isAggFunctionAllowed("avg")).isTrue();
        assertThat(whitelist.isAggFunctionAllowed("MAX")).isTrue();
        assertThat(whitelist.isAggFunctionAllowed("MIN")).isTrue();
        assertThat(whitelist.isAggFunctionAllowed("SUM")).isTrue();
    }

    @Test
    void fonctionsDangereusesSontRejetees() {
        assertThat(whitelist.isAggFunctionAllowed("DROP")).isFalse();
        assertThat(whitelist.isAggFunctionAllowed("DELETE")).isFalse();
        assertThat(whitelist.isAggFunctionAllowed(null)).isFalse();
    }

    @Test
    void limiteParDefautAppliqueeSiNonSpecifiee() {
        assertThat(whitelist.safeLimit(null)).isEqualTo(20);
        assertThat(whitelist.safeLimit(0)).isEqualTo(20);
        assertThat(whitelist.safeLimit(-5)).isEqualTo(20);
    }

    @Test
    void limitePlafonneeA50MemeSiDemandePlusGrande() {
        assertThat(whitelist.safeLimit(10000)).isEqualTo(50);
        assertThat(whitelist.safeLimit(51)).isEqualTo(50);
    }

    @Test
    void limiteRaisonnableEstRespectee() {
        assertThat(whitelist.safeLimit(10)).isEqualTo(10);
    }
}   