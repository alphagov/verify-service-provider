# 12. We will use the "full" profile

Date: 2017-07-25

## Status

Accepted

## Context

Verify's SAML profile specifies that Responses and Assertions should be signed.
Responses should be signed by the Verify Hub and Assertions should be signed by
the Matching Service Adapter.

This profile causes problems with some off-the-shelf SAML service providers,
which can't handle multiple signatures from different keys in the same message.
As a workaround, Verify introduced a "simple" profile where we do not sign Responses.

## Decision

We will use the "full" profile, not the "simple" profile. The hub will sign responses
and the service provider will validate them against the hub's metadata.

## Consequences

The verify-service-provider will need to request metadata from hub, so it will need a
bit more configuration.

We'll have to do a bit more work to handle responses correctly.

Relying parties using the verify-service-provider will be more secure.

