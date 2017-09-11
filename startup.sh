#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

./gradlew installDist

./build/install/verify-service-provider/bin/verify-service-provider server ./configuration/verify-service-provider.yml
