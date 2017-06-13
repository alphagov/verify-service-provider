#!/usr/bin/env bash

# something about using gradles distZip to:
./gradlew distZip

tar -xf build/distributions/verify-service-provider.zip -C build/distributions/

./build/distributions/verify-service-provider/bin/verify-service-provider server ./configuration/verify-service-provider.yml
