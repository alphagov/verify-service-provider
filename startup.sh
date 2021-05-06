#!/usr/bin/env bash
set -e

CONFIG_FILE=./verify-service-provider.yml

case $1 in
    'local-fed')
        CONFIG_FILE=./local-running/local-config.yml
        if test -e local.env; then
            set -a
            # shellcheck disable=SC1091
            source local.env
            set +a
        else
            printf "%sNo local environment found. Use verify-local-startup or openssl to generate a local.env file\n%s" "$(tput setaf  1)" "$(tput sgr0)"
        fi
        ;;
    'vsp-only')
        # shellcheck disable=SC1091
        source ./local-running/local-vsp-only.env
        ;;
esac

cd "$(dirname "$0")"

./gradlew installDist

./build/install/verify-service-provider/bin/verify-service-provider server $CONFIG_FILE
