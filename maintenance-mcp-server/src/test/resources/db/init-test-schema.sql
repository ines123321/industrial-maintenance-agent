CREATE TABLE composants (
    id                        VARCHAR(50) PRIMARY KEY,
    nom                       VARCHAR(200) NOT NULL,
    type                      VARCHAR(100) NOT NULL,
    seuil_critique            NUMERIC(10, 2),
    unite                     VARCHAR(20),
    frequence_maintenance_h   INTEGER,
    localisation              VARCHAR(200)
);

CREATE TABLE codes_erreur (
    code                VARCHAR(20) PRIMARY KEY,
    composant_id        VARCHAR(50) REFERENCES composants(id),
    cause               VARCHAR(500) NOT NULL,
    action_corrective   VARCHAR(500) NOT NULL,
    niveau_urgence      VARCHAR(20)
);

CREATE TABLE historique_interventions (
    id                   SERIAL PRIMARY KEY,
    composant_id         VARCHAR(50) REFERENCES composants(id),
    date_intervention    DATE NOT NULL,
    type_intervention    VARCHAR(100) NOT NULL,
    technicien           VARCHAR(100),
    notes                VARCHAR(500)
);

INSERT INTO composants (id, nom, type, seuil_critique, unite, frequence_maintenance_h, localisation) VALUES
('M12', 'Moteur asynchrone ligne A', 'moteur', 85.00, '°C', 2000, 'Atelier 1'),
('C3',  'Compresseur principal',     'compresseur', 8.50, 'bar', 1000, 'Local technique'),
('VF7', 'Variateur convoyeur',       'variateur', 50.00, 'Hz', 1500, 'Atelier 2');

INSERT INTO codes_erreur (code, composant_id, cause, action_corrective, niveau_urgence) VALUES
('F0002', 'VF7', 'Surcharge du variateur de fréquence détectée', 'Couper l''alimentation, vérifier la charge', 'critique'),
('E1015', 'M12', 'Température moteur au-dessus du seuil', 'Arrêter le moteur, vérifier la ventilation', 'critique');