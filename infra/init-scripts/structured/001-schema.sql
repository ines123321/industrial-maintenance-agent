-- ============================================================
-- Tables métier : maintenance industrielle
-- ============================================================

CREATE TABLE composants (
    id                        VARCHAR(50) PRIMARY KEY,
    nom                       VARCHAR(200) NOT NULL,
    type                      VARCHAR(100) NOT NULL,  -- moteur, convoyeur, compresseur, variateur...
    seuil_critique            NUMERIC(10, 2),
    unite                     VARCHAR(20),             -- bar, °C, Hz, A...
    frequence_maintenance_h   INTEGER,                 -- fréquence de maintenance préventive, en heures
    localisation              VARCHAR(200)
);

CREATE TABLE codes_erreur (
    code                VARCHAR(20) PRIMARY KEY,
    composant_id        VARCHAR(50) REFERENCES composants(id),
    cause               VARCHAR(500) NOT NULL,
    action_corrective   VARCHAR(500) NOT NULL,
    niveau_urgence      VARCHAR(20) CHECK (niveau_urgence IN ('info', 'warning', 'critique'))
);

CREATE TABLE historique_interventions (
    id                   SERIAL PRIMARY KEY,
    composant_id         VARCHAR(50) REFERENCES composants(id),
    date_intervention    DATE NOT NULL,
    type_intervention    VARCHAR(100) NOT NULL,  -- preventive, corrective, inspection
    technicien           VARCHAR(100),
    notes                VARCHAR(500)
);

-- ============================================================
-- Rôle read-only dédié au MCP server (défense en profondeur :
-- même si la couche applicative est contournée, ce rôle ne
-- peut physiquement ni écrire ni supprimer).
-- ============================================================

DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'mcp_readonly') THEN
        CREATE ROLE mcp_readonly WITH LOGIN PASSWORD 'mcp_readonly_dev_password';
    END IF;
END
$$;

GRANT CONNECT ON DATABASE maintenance TO mcp_readonly;
GRANT USAGE ON SCHEMA public TO mcp_readonly;
GRANT SELECT ON composants, codes_erreur, historique_interventions TO mcp_readonly;

-- Timeout de sécurité : évite qu'une requête mal formée bloque la base
ALTER ROLE mcp_readonly SET statement_timeout = '5s';