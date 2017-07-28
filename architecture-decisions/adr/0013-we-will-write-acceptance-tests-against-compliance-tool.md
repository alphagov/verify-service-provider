# 13. We will write acceptance tests against compliance tool

Date: 2017-07-25

## Status

Accepted

## Context

Verify provide a "compliance tool" which allows relying parties to check that their
implementations are compatible with the SAML profile of Verify.

Currently relying parties must test their service against the compliance tool as part
of the onboarding process.

By writing tests against the compliance tool we can:
* make sure our own implementation is compliant
* demonstrate to relying parties how to write tests against the compliance tool
* learn more about the user experience of using the compliance tool

## Decision

We'll write acceptance tests against the compliance tool for the verify service provider
and the stub relying parties.

Tests for the service provider will give us direct feedback on whether it's compliant.
Tests for the stub relying parties will give us confidence they work end-to-end and should
provide a template for real relying parties to write their own tests.

## Consequences

We'll have to write some tests.

