# 22. We will not validate the recipient

Date: 2017-09-26

## Status

Accepted

## Context

The subject confirmation data on assertions has a recipient attribute, which the MSA sets to the assertion consumer service url. The assertions also have an audience restriction, which is set to the service entity id.

The verify service provider, conceptually, should not need to know the assertion consumer service url - as this ties somewhat to the relying party's infrastructure, and makes it difficult to change. Knowing it would also lead to more complicated configuration for relying parties as we introduce multi tenancy to the verify service provider.

In usual usages of SAML, the recipient should be validated, as should the audience restriction (if present), and these might be different and have slightly different meanings. However, since we control the production of this SAML (via the MSA), we know that the audience restriction will always be present and set to the service entity id, so validating this will mitigate any potential issues from not validating the recipient.

## Decision

The verify service provider will not validate the assertion consumer service url.

## Consequences

We will not be performing a check that the SAML specification says should be performed (however, as above, we believe this is covered by the audience restriction check).
We will have to revert some commits already merged.