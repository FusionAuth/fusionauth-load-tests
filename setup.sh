#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

function validation() {
  echo ""
  if [[ -z ${FA_URL:-} ]]; then
    echo "⛔️ FA_URL is not set"
    echo "Export this variable and try again. See the README for more info."
    exit 1
  else
    echo "✅ FA_URL = ${FA_URL}"
  fi

  if [[ -z ${FA_API_KEY:-} ]]; then
    echo "⛔️ FA_API_KEY is not set"
    echo "Export this variable and try again. See the README for more info."
    exit 1
  else
    echo "✅ FA_API_KEY = ${FA_API_KEY}"
  fi

  if [[ -z ${FA_TENANT_ID:-} ]]; then
    echo "⚠️ FA_TENANT_ID is not set"
    echo "Using the default value. See the README for more info."
    FA_TENANT_ID="efb21cfc-fa60-46f4-9598-889151e58517"
  else
    echo "✅ FA_TENANT_ID = ${FA_TENANT_ID}"
  fi
}

function set_parser() {
  # Try to find a JSON parser.
  if command -v jq &>/dev/null; then
    PARSER=jq
  elif command -v python3 &>/dev/null; then
    PARSER="python3 -m json.tool -"
  elif command -v json_pp &>/dev/null; then
    PARSER=json_pp
  else
    PARSER="cat -"
  fi

  if [[ ${PARSER} == "cat -" ]]; then
    echo "⚠️ Could not find a JSON parser. JSON output will be unformatted."
  else
    echo "✅ JSON parser = ${PARSER}"
  fi
}

function create_application() {
  echo -e "\nCreating Application 'FusionAuthLoadTesting'"

  curl -s -X POST -H "Content-Type: application/json" -H "Authorization: ${FA_API_KEY}" \
    -H "X-FusionAuth-TenantId: ${FA_TENANT_ID}" -H "Cache-Control: no-cache" \
    -d \
    '{
     "application": {
       "name": "FusionAuthLoadTesting",
       "oauthConfiguration": {
          "authorizedRedirectURLs": ["https://acme.com/redirect"],
          "clientSecret": "ZDA5Yzk0NTUtYzg4ZS00ZmNhLThmOGUtZDFkN2M3YTllNDY1",
          "enabledGrants": ["authorization_code"]
       },
       "roles": ["admin", "user"]
     }
  }' "${FA_URL}/api/application/11e7ea7b-784d-4687-bf2d-4f8ee479a4dd" | $PARSER
}

function main() {
  validation
  set_parser
  create_application
}

main
