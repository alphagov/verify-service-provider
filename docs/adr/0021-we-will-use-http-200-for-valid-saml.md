# 21. We will put verified status inside json object

Date: 2017-08-16

## Status

Accepted

## Context

When communicating with the Verify Service Provider API, we need to decide what status code to respond with
for correctly formatted SAML that represents some kind of authentication failure (eg. NO_MATCH).


## Decision

Any valid SAML will return a 200 OK response and can be deserialized as a <code>TranslatedRepsonseBody</code>.
We will have to define an enum of possible SAML outcomes (<code>Scenario</code>) as we can't use HTTP codes
Invalid JSON/SAML or internal errors will use a relevant, different HTTP status code.

## Consequences

API consumers such as passport-verify will have to handle authentication failures as 200 codes. We will
have to document the possible error scenarios. We can add API authentication such as HTTP Basic Auth at a
later date without worrying about clashing on HTTP status codes.
