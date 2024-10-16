#!/usr/bin/env bash

#
# Copyright (c) 2024, FusionAuth, All Rights Reserved
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
# either express or implied. See the License for the specific
# language governing permissions and limitations under the License.
#

set -o errexit
set -o nounset
set -o pipefail

function show_help() {
  echo -e "\nUsage: $(basename "${0}") [options]\n"
  echo -e "    -h --help      Show this help message"
  echo -e "    -u --url       Required, URL of your FusionAuth instance"
  echo -e "    -k --key       Required, FusionAuth API key"
  echo -e "    -t --tenant    Optional, FusionAuth Tenant ID"
  exit
}

function argerr() {
  echo -e "\n⛔️ ${1}"
  exit 1
}

function parse_args() {
  while [[ $# -gt 0 ]]; do
    case "${1}" in
    -h | --help) show_help ;;
    -u | --url)
      shift
      if (($# < 1)); then argerr "--url requires the URL of your FusionAuth instance"; fi
      FA_URL="${1:-}"
      shift
      ;;
    -k | --key)
      shift
      if (($# < 1)); then argerr "--key requires a valid FusionAuth API key"; fi
      FA_API_KEY="${1:-}"
      shift
      ;;
    -t | --tenant)
      shift
      if (($# < 1)); then argerr "--tenant requires your FusionAuth Default tenant ID"; fi
      FA_TENANT_ID="${1:-}"
      shift
      ;;
    *) argerr "Unsupported flag '${1}'" ;;
    esac
  done

  echo ""

  if [[ -z ${FA_URL:-} ]]; then
    FA_URL="https://local.fusionauth.io"
    echo "⚠️ --url was not set, using default value"
  else
    if ! [[ ${FA_URL} == "http"* ]]; then
      echo "⛔️ URL must be in the format https://your.fusionauth.host"
      exit 1
    fi
  fi
  echo "✅ URL = ${FA_URL}"

  if [[ -z ${FA_API_KEY:-} ]]; then
    FA_API_KEY="bf69486b-4733-4470-a592-f1bfce7af580"
    echo "⚠️ --key was not set, using default value"
  fi
  echo "✅ API Key = ${FA_API_KEY}"

  if [[ -z ${FA_TENANT_ID:-} ]]; then
    FA_TENANT_ID="efb21cfc-fa60-46f4-9598-889151e58517"
    echo "⚠️ --tenant was not set, using default value"
  fi
  echo "✅ Tenant ID = ${FA_TENANT_ID}"
}

function set_json_parser() {
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

function check_connection() {
  echo -e "\nChecking connection"

  if ! RESPONSE=$(curl -X GET -s -o /dev/null -w "%{http_code}" \
    -H "Content-Type: application/json" \
    -H "Authorization: ${FA_API_KEY}" \
    -H "X-FusionAuth-TenantId: ${FA_TENANT_ID}" \
    -H "Cache-Control: no-cache" \
    "${FA_URL}/api/tenant/${FA_TENANT_ID}"); then
    echo "⛔️ Cannot connect to ${FA_URL}"
    exit 1
  fi

  if [[ ${RESPONSE} == "200" ]]; then
    echo "✅ OK!"
  else
    echo "⛔️ Got a ${RESPONSE} response code"
    echo "  - Check that the API key is correct, and has necessary permissions"
    echo "  - Check that the tenant ID for the 'Default' tenant is correct"
    exit 1
  fi
}

function create_application() {
  echo -e "\nCreating Application 'FusionAuthLoadTesting'"

  curl -s -S -X POST \
    -H "Content-Type: application/json" \
    -H "Authorization: ${FA_API_KEY}" \
    -H "X-FusionAuth-TenantId: ${FA_TENANT_ID}" \
    -H "Cache-Control: no-cache" \
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

function update_tests() {
  echo -e "\nUpdating test files..."

  TESTS_DIR=build/dist

  echo "- User Registrations"
  sed -i -e "s#https://local.fusionauth.io#${FA_URL}#g" "${TESTS_DIR}/User-Registrations.json"
  sed -i -e "s#bf69486b-4733-4470-a592-f1bfce7af580#${FA_API_KEY}#g" "${TESTS_DIR}/User-Registrations.json"
  sed -i -e "s#efb21cfc-fa60-46f4-9598-889151e58517#${FA_TENANT_ID}#g" "${TESTS_DIR}/User-Registrations.json"

  echo "- User Logins"
  sed -i -e "s#https://local.fusionauth.io#${FA_URL}#g" "${TESTS_DIR}/User-Logins.json"
  sed -i -e "s#bf69486b-4733-4470-a592-f1bfce7af580#${FA_API_KEY}#g" "${TESTS_DIR}/User-Logins.json"

  echo """
We have only modified two of the available tests with your authentication data.
This may be sufficient for your needs. If not, see the README for information
on how to modify tests.
"""
}

function main() {
  parse_args "$@"
  set_json_parser
  check_connection
  create_application
  echo -e "\nRunning Savant to build the tests..."
  if command -v sb &>/dev/null; then
    sb int
  else
    echo -e "\n⛔️ Savant is not installed. See the README for installation instructions"
    exit 1
  fi
  update_tests
}

main "$@"
