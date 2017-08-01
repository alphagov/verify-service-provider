# 18. We will have hubSSOLocation as a configuration option

Date: 2017-08-01

## Status

Pending

## Context

Verify Service Provider needs to know the SSO location for Verify Hub in order
to generate a valid AuthnRequest. 

Hub Metadata does not provide SSOLocation because if it would, it would have to
describe Hub as an IDP and therefore provide signing certificates. Having signing
certificates in the Hub metadata makes Relying Parties trust assertions signed by
hub, which would break the security contract between the Matching Service Adapter
and the Hub.

Matching Service Adapter does provide Hub SSOLocation in its metadata. However,
the value is directly copied in from the Matching Service Adapter configuration.
Matching Service Adapter itself does not use the value, therefore we do not
trust this feature to remain as part of the Matching Service Adapter.

## Decision

We will provide the hub sso-location as a configuration option instead of parsing
it from the msa-metadata.

## Consequences

Relying parties will need to spend extra effort to configure the hubSSOLocation.

