# 9. We will not use a dependency injection framework

Date: 2017-07-25

## Status

Accepted

## Context

We're writing a really small project and we don't think that a
framework like Guice will provide enough benefit to outweigh the cost.

## Decision

We will use constructor injection, but we won't use a DI framework. We'll use a
factory object instead.  We'll avoid calling new inside constructors where
practical.

## Consequences

We'll have to wire up factories manually.
