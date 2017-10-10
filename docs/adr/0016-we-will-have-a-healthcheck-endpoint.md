# 16. We will have a healthcheck endpoint

Date: 2017-07-25

## Status

Accepted

## Context

In various user research sessions we've observed users start the MSA or the verify service provider
and then want to check whether it's working correctly. There's also a need for users to be able to
monitor the health of the system once it's deployed to their environment.

Dropwizard allows you to configure an HTTP endpoint as a healthcheck. This can perform some arbitrary
actions that check the health of the system.

## Decision

We will have a healthcheck endpoint that will check the verify-service-provider can read metadata from
the hub and the MSA.

## Consequences

Users will be able to check and monitor the health of the verify-service-provider.

We will have to add some healthchecks.

