#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"
curl https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/service-test-data --data @compliance-tool-init.json --header 'Content-Type: application/json'
echo

