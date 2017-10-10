# 15. We will depend on a minimal set of verify SAML libs

Date: 2017-07-25

## Status

Accepted

## Context

Verify have a number of pre-existing (closed source) libraries at various
levels of abstraction for handling SAML behaviour. By depending on these
we can benefit from all the work that's already been done making these easy
to use and secure by default.

There's a plan in the medium term to open source a lot of these libraries.
By depending on as few of them as possible we should be able to call the
verify-service-provider "fully open" sooner.

Some of the libraries at higher levels of abstraction are of questionable
value - although they make it easy to be consistent with the rest of verify
they abstract away the exact nature of the SAML which makes the code hard to
read.

## Decision

Good libraries that we should use for now:

* saml-serialisers
* ida-saml-extensions
* saml-security
* saml-metadata-bindings

Libraries we think we should ignore

* hub-saml

## Consequences

We may have to inline some bits of e.g. hub-saml, or reimplement them differently.
