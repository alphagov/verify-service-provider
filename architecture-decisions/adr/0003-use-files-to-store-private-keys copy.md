# 3. Use files to store private keys

Date: 01/06/2017

## Status

Pending

## Context

Users (RPs) will need to provide some private keys to sign AuthnRequests and
decrypt Response Assertions.

They will need to provide these to the verify-service-provider in some, reasonably
secure way. Different users may have different opinions on how best to do this.

## Decision

Initially we'll use files for this.

We chose not to use environment variables because they're visible to other processes.
We chose not to use a more complicated solution because it would be more complicated.

## Consequences

Users will have to provide private keys as files.

