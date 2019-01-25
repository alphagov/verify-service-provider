#!/usr/bin/env bash
set -e

ROOT_DIR="$(dirname "$0")"
cd "$ROOT_DIR"

function cleanup {
  rm -rf "$ROOT_DIR/work"
}
trap cleanup EXIT


cfLogin() {
  if [ -z "${CF_USER:-}" ]; then
    echo "Using cached credentials in ${CF_HOME:-home directory}" >&2
  else
    CF_API="${CF_API:?CF_USER is set - CF_API environment variable also needs to be set}"
    CF_ORG="${CF_ORG:?CF_USER is set - CF_ORG environment variable also needs to be set}"
    CF_SPACE="${CF_SPACE:?CF_USER is set - CF_SPACE environment variable also needs to be set}"
    CF_PASS="${CF_PASS:?CF_USER is set - CF_PASS environment variable also needs to be set}"

    # CloudFoundry will cache credentials in ~/.cf/config.json by default.
    # Create a dedicated work area to avoid contaminating the user's credential cache
    export CF_HOME="$ROOT_DIR/work"
    rm -rf "$CF_HOME"
    mkdir -p "$CF_HOME"
    echo "Authenticating to CloudFoundry at '$CF_API' ($CF_ORG/$CF_SPACE) as '$CF_USER'" >&2
    cf api "$CF_API"
    # Like 'cf login' but for noninteractive use
    cf auth "$CF_USER" "$CF_PASS"
    cf target -o "$CF_ORG" -s "$CF_SPACE"
  fi
}

./pre-commit.sh

./gradlew clean distZip

cfLogin
cf set-env verify-service-provider-integration SAML_SIGNING_KEY "$TEST_RP_PRIVATE_SIGNING_KEY"
cf set-env verify-service-provider-integration SAML_PRIMARY_ENCRYPTION_KEY "$TEST_RP_PRIVATE_ENCRYPTION_KEY"
cf push -f integration-manifest.yml -p build/distributions/verify-service-provider-*.zip

