#!/usr/bin/env bash
set -e

# Use the local environment if one exists
if test -e local.env; then
    source local.env
else
    printf "$(tput setaf 1)No local environment found. Use verify-local-startup or openssl to generate a local.env file\n$(tput sgr0)"
fi

cd "$(dirname "$0")"

./gradlew installDist

./build/install/verify-service-provider/bin/verify-service-provider server ./verify-service-provider.yml
