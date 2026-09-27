#!/usr/bin/env bash
# Usage: update-security-json.sh <nom_outil> <critical> <high> <medium> <low> <status>
set -euo pipefail

NOM="$1"; CRIT="$2"; HIGH="$3"; MED="$4"; LOW="$5"; STATUT="$6"
FICHIER="reports/security.json"
SHA="${GITHUB_SHA:-local}"
SHA_COURT="${SHA:0:7}"
DATE=$(date +%F)

mkdir -p reports
if [ ! -f "$FICHIER" ]; then
  echo '{"lastUpdated":"","tools":[]}' > "$FICHIER"
fi

jq --arg nom "$NOM" --argjson crit "$CRIT" --argjson high "$HIGH" \
   --argjson med "$MED" --argjson low "$LOW" --arg statut "$STATUT" \
   --arg date "$DATE" --arg sha "$SHA_COURT" '
  .lastUpdated = $date |
  .tools = (
    (.tools // []) | map(select(.name != $nom))
  ) + [{
    name: $nom, critical: $crit, high: $high, medium: $med, low: $low,
    status: $statut, updatedAt: $date, commitSha: $sha
  }]
' "$FICHIER" > "$FICHIER.tmp" && mv "$FICHIER.tmp" "$FICHIER"

echo "security.json mis à jour pour $NOM"
cat "$FICHIER"