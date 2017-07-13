#!/usr/bin/env bash

# something about using gradles distZip to:
./gradlew installDist 

./build/install/verify-service-provider/bin/verify-service-provider server ./configuration/verify-service-provider.yml
