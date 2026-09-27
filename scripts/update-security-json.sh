#!/usr/bin/env bash
# Usage: update-security-json.sh <nom_outil> <type: SAST|DAST> <critical> <high> <medium> <low> <status>
set -euo pipefail

NOM="$1"; TYPE="$2"; CRIT="$3"; HIGH="$4"; MED="$5"; LOW="$6"; STATUT="$7"
FICHIER="reports/security.json"
SHA="${GITHUB_SHA:-local}"
SHA_COURT="${SHA:0:7}"
DATE=$(date +%F)

mkdir -p reports
if [ ! -f "$FICHIER" ]; then
  echo '{"lastUpdated":"","tools":[]}' > "$FICHIER"
fi

jq --arg nom "$NOM" --arg type "$TYPE" --argjson crit "$CRIT" --argjson high "$HIGH" \
   --argjson med "$MED" --argjson low "$LOW" --arg statut "$STATUT" \
   --arg date "$DATE" --arg sha "$SHA_COURT" '
  .lastUpdated = $date |
  .tools = (
    (.tools // []) | map(select(.name != $nom))
  ) + [{
    name: $nom, type: $type, critical: $crit, high: $high, medium: $med, low: $low,
    status: $statut, updatedAt: $date, commitSha: $sha
  }]
' "$FICHIER" > "$FICHIER.tmp" && mv "$FICHIER.tmp" "$FICHIER"

echo "security.json mis à jour pour $NOM"