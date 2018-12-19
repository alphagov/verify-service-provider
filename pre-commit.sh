#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"
./gradlew clean test testFeature testAcceptance
./gradlew dependencyUpdates

