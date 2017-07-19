# 5. SP will generate request ID

Date: 01/06/2017

## Status

Pending

## Context

AuthnRequests contain an ID attribute the value of which will be sent back in
the Response as an "InResponseTo" attribute.

Something needs to decide what the value of the ID is, and something needs to validate that the InResponseTo is the same as we expected.

## Decision

The service provider will generate a random GUID to use as the AuthnRequest ID.

## Consequences

Users won't be able to customise their IDs.
