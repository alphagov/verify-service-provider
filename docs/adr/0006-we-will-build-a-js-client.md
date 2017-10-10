# 6. We will build a JS client

Date: 2017-06-01

## Status

Accepted

## Context

At least one user is currently using node js and passport. We want to provide as
frictionless as possible an integration for them.

Other users will be using other languages and frameworks.

## Decision

We will initially build only a node / passport client. We will want to build
another client in another language as soon as possible to make sure the API
is well designed.

Users should also be able to interact with the API directly if we haven't built
an appropriate client for their use case.

## Risks

We will have to learn how passport works.

## Consequences

JS client lives here https://github.com/alphagov/passport-verify
