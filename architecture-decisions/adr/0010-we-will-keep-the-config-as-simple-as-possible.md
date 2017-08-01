# 10. We will keep the config as simple as possible

Date: 2017-07-25

## Status

Accepted

## Context

Existing verify configuration for things like metadata can be quite
intimidatingly complex. We'd like the application to be as simple as
possible to configure.

## Decision

We'll try to make the config have sane defaults wherever possible
so that the user doesn't have to specify things they shouldn't care
about.

The user shouldn't have to specify things we don't use (e.g. truststorePath).

## Consequences

We may have to modify some library classes to make them more flexible.

The MSA has some defaults, so we could use this as an example.
