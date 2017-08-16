# 17. We will pass the request ID to the client

Date: 2017-07-25

## Status

Accepted

## Context

Verify will send the same request ID that it sees in the AuthnRequest to the
MSA when it does matching.  If the service is able to see the request ID in a
convenient form then they are able to correlate MSA attribute query requests
with AuthnRequests they've sent.

We also need to be able to validate that the InResponseTo attribute on the
Response matches the ID we sent in the AuthnRequest. So long as the service is
able to store the ID in a secure way (i.e. without the possibility of a
malicious user changing it in their session) it should be fine for them to send
this back to the service to validate directly.

There was also discussion about sending an HMAC of the ID back to the client be
stored in session - this would mitigate against attacks due a relying party
storing the ID insecurely.

## Decision

We'll return the request ID alongside the AuthnRequest when we generate it and
ask for it when translating Responses.

## Consequences

Relying parties will need to store the request ID in session securely and send
it back to the verify-service-provider when handling responses.

