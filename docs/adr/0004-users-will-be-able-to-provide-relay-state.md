# 4. Users will be able to provide relay state

Date: 2017-06-01

## Status

Pending (may want to change if the added complexity is high)

## Context

In SAML RPs can provide some extra data along with the request. This is
called RelayState. Some existing RPs use this, but we're not sure what
they use it for.

We're not aware of any need for the service-provider to use relay state itself.

## Decision

Users will be able to specify whatever relay state they want to and it will be
provided in the response.

## Consequences

* The service provider won't be able to use relay state itself
* The user will need to be able to customize one of the form inputs
* The client-side (node JS) code will need to provide some way of getting
  relay state from the response.
