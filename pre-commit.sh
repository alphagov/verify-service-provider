#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"
./gradlew clean test testAcceptance
./gradlew dependencyUpdates

