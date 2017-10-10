# 20. We will put verified status inside json object

Date: 2017-08-09

## Status

Accepted

## Context

When receiving a User Account Creation response from the MSA, the response contains the attributes required for account creation.
A User Account Creation SAML object contains *_VERIFIED attributes which are siblings of the attribute they reference.


## Decision

The Verify Service Provider will require the service to have requested both an attribute and its
corresponding verified attribute or it will error. This is already enforced by the integration environment form.
The JSON the VSP returns will group the attribute value and verified status together.

## Consequences

Any VSP consumers will need to update as this is a breaking interface change. At time of writing this is only
passport-verify.

