# 11. We will use a secure token to validate InResponseTo

Date: 2017-07-25

## Status

Pending

## Context

A SAML relying party must check that responses it receives have an InResponseTo
attribute that matches the ID of a request that the relying party sent for this
user.

Since we're trying to keep the verify service provider as stateless as possible
we don't want it to have to remember which IDs belong to which users. Instead
we'd like to delegate this behaviour to the client of the service provider
(i.e. the service itself), which will have its own session store.

If a relying party's session store was insecure (for example and unsigned
session cookie) then an attacker could circumvent the InResponseTo check by
setting the value in session to something else. To mitigate against this attack
the verify service provider could pass the service back a HMAC of the ID
(either alongside the ID or instead of it).

## Decision

We won't implement this for now because initially we're happy that our relying
parties will store the session securely. We may want to implement it later as
an extra level of security.

## Consequences

We will defer implementing the "secure token" until later in the project.

