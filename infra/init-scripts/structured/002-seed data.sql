-- ============================================================
-- Composants
-- ============================================================

INSERT INTO composants (id, nom, type, seuil_critique, unite, frequence_maintenance_h, localisation) VALUES
('M12',  'Moteur asynchrone ligne A',       'moteur',       85.0,  '°C', 2000, 'Atelier 1 - Ligne A'),
('C3',   'Compresseur principal',           'compresseur',  8.5,   'bar', 1000, 'Local technique'),
('VF7',  'Variateur de fréquence convoyeur','variateur',    50.0,  'Hz',  1500, 'Atelier 2 - Convoyeur C7'),
('CV7',  'Convoyeur à bande C7',            'convoyeur',    NULL,  NULL,  500,  'Atelier 2'),
('CAP4', 'Capteur de pression ligne B',     'capteur',      10.0,  'bar', 3000, 'Atelier 1 - Ligne B');

-- ============================================================
-- Codes erreur
-- ============================================================

INSERT INTO codes_erreur (code, composant_id, cause, action_corrective, niveau_urgence) VALUES
('F0002', 'VF7', 'Surcharge du variateur de fréquence détectée',
          'Couper l''alimentation, vérifier la charge mécanique du convoyeur, contrôler les roulements avant redémarrage',
          'critique'),
('E1015', 'M12', 'Température moteur au-dessus du seuil (85°C)',
          'Arrêter le moteur, vérifier le système de ventilation, contrôler l''état des roulements',
          'critique'),
('W0043', 'C3',  'Pression en dessous du seuil bas attendu',
          'Vérifier l''étanchéité du circuit, contrôler le filtre à air d''admission',
          'warning'),
('I0007', 'CAP4','Dérive de calibration détectée sur le capteur',
          'Planifier un recalibrage du capteur lors de la prochaine maintenance préventive',
          'info');

-- ============================================================
-- Historique d'interventions
-- ============================================================

INSERT INTO historique_interventions (composant_id, date_intervention, type_intervention, technicien, notes) VALUES
('M12', '2026-06-15', 'preventive', 'A. Ben Salah', 'Graissage roulements, contrôle vibratoire OK'),
('C3',  '2026-07-02', 'corrective', 'M. Trabelsi',  'Remplacement joint d''étanchéité suite fuite mineure'),
('VF7', '2026-08-20', 'inspection', 'A. Ben Salah', 'Contrôle standard, RAS'),
('CV7', '2026-09-01', 'preventive', 'S. Gharbi',    'Tension et alignement de la bande');