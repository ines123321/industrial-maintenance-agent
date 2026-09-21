# Guide de diagnostic — Variateurs de fréquence

## Généralités

Ce guide couvre la démarche de diagnostic à suivre en cas de code erreur
affiché sur un variateur de fréquence, avant intervention corrective.
Pour la cause et l'action corrective précises d'un code erreur donné,
consulter la base de données des codes erreur (table `codes_erreur`).

## Démarche de diagnostic générale

1. **Relever le code erreur exact** affiché sur l'écran du variateur,
   ainsi que l'heure d'apparition.
2. **Ne pas réinitialiser le variateur** avant d'avoir identifié la cause
   probable — une réinitialisation immédiate peut masquer un défaut
   récurrent.
3. **Vérifier les conditions de charge mécanique** en amont (convoyeur,
   accouplement) avant de suspecter le variateur lui-même.
4. **Consulter l'historique d'interventions** du composant concerné pour
   identifier une éventuelle récurrence.
5. **Appliquer l'action corrective** associée au code erreur, après
   consignation de l'équipement.

## Niveaux d'urgence

- **Critique** : arrêt immédiat requis, intervention prioritaire.
- **Warning** : surveillance renforcée, planifier une intervention à
  court terme.
- **Info** : à traiter lors de la prochaine maintenance préventive
  planifiée, sans urgence opérationnelle.

## Important

Ce document ne contient pas les valeurs de seuils numériques (température,
pression, fréquence) propres à chaque composant — ces valeurs doivent être
consultées via les outils de requête sur la base de données de maintenance,
jamais estimées ou approximées.