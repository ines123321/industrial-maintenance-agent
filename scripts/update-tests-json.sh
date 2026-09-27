#!/usr/bin/env bash
# Usage: update-tests-json.sh <nom_module> <total> <passed> <failed> <skipped>
set -euo pipefail

NOM="$1"; TOTAL="$2"; PASSED="$3"; FAILED="$4"; SKIPPED="$5"
FICHIER="reports/tests.json"
SHA="${GITHUB_SHA:-local}"
SHA_COURT="${SHA:0:7}"
DATE=$(date +%F)

mkdir -p reports
if [ ! -f "$FICHIER" ]; then
  echo '{"lastUpdated":"","commitSha":"","modules":[]}' > "$FICHIER"
fi

jq --arg nom "$NOM" --argjson total "$TOTAL" --argjson passed "$PASSED" \
   --argjson failed "$FAILED" --argjson skipped "$SKIPPED" \
   --arg date "$DATE" --arg sha "$SHA_COURT" '
  .lastUpdated = $date |
  .commitSha = $sha |
  .modules = (
    (.modules // []) | map(select(.name != $nom))
  ) + [{
    name: $nom, total: $total, passed: $passed, failed: $failed, skipped: $skipped
  }]
' "$FICHIER" > "$FICHIER.tmp" && mv "$FICHIER.tmp" "$FICHIER"

echo "tests.json mis à jour pour $NOM"
cat "$FICHIER"