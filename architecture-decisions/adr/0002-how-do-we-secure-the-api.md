# 2. Use dropwizard functionality to secure API

Date: 2017-06-01

## Status

Accepted

## Context

We need to secure the interaction between the "client" code (e.g. node JS)
and the server side code (which will be a dropwizard app).

Depending on how the users want to run the service provider we may need
different security solutions.

## Decision

If possible users can talk to the service provider on the loopback (127.0.0.1)
If that doesn't work for some reason then they can use the dropwizard config
to set up basic auth or tls or something.

See http://www.dropwizard.io/1.1.0/docs/manual/configuration.html#connectors

## Consequences

We'll deliver a prototype that allows users to configure dropwizard things.

