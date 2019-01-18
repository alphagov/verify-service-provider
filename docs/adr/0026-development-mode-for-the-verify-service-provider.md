# 26. Development mode for the Verify Service Provider

Date: 2019-01-18

## Status

Accepted

Supersedes [8. Provide an End To End Stub](0008-provide-an-end-to-end-stub.md)

## Context

Interacting with the Compliance Tool (CT) is not a straightforward process and the API is not necessarily straightforward. When a user of the VSP is using it for the first time having to configure and run the VSP as well as understand how the CT API works can be burdensome. They need to:
1. Create a set of private keys and certificates
2. Configure the VSP to interact with the CT and use the newly generated keys
   and certificates
3. Create a script that can initiate a session with the CT
4. Create a client that can interact with the VSP
5. Test the interaction between the client, VSP, and CT

## Decision

The VSP will providea development mode initialized by a Dropwizard command that
will run the VSP in a development mode. When starting in this mode the VSP will
create its own keys and certificates, as well as some other config, that it will
use to initialize a new session with the compliance tool. 

A user of the VSP will be able start development mode by running the following
command from the shell:

```
./bin/verify-service-provider development
```

The command will provide additional options to the use so that the user can
control:
- the host the VSP will bind to
- the port the VSP will run on
- the location where the CT will send SAML responses to
- the contents of the matching dataset that the CT will use in SAML responses

In order to simplify the implementation of this mode the behaviour will only be
available to users of the 'non-matching' journey.

## Consequences

Supersedes [ADR-0008](docs/adr/0008-provide-an-end-to-end-stub.md)
