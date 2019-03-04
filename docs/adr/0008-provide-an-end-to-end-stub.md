# 8. Provide an End To End Stub

Date: 2017-06-12

## Status

Accepted

Superseded by [26. Development mode for the Verify Service Provider](0026-development-mode-for-the-verify-service-provider.md)

## Context

We wish to run regular user research and testing against the prototype Service Provider.

To support user research we need to provide a user journey that resembles a typical Verify journey.

The area we are most interested in is the interface between the Service and the Hub.

## Decision

We will create a Stub Verify Hub that will allow for end-to-end testing of the prototype.

It will not provide a SAML implementation.

It will expect a form submission on a web resource that mimics the behaviour of receiving an AuthnRequest.

If the form post is successful then a browser redirect will be issued to a page explaining where the user is in their
journey.  Continuing from this page will take the user to a page containing a series of possible response scenarios that
can be selected.

Any information that needs to be stored from the original request or between pages will be stored in a session
cookie. The session cookie does not need to be signed or encrypted initially.

Some scenarios will require additional user input such as a providing a pid value or user account creation attributes.
These will be provided on subsequent pages. In every case the user will need to provide the URL that the stub-hub should
send its responses to.

Each response will mimic the shape of a Hub SAML Response form that can be submitted back to the Service and SP
prototype.

The details of each canned response will be encoded as base64 encoded JSON in the SAMLResponse parameter of the form.

The prototype service provider will be able to understand each canned response and produce a specific response to the
Service/client.

Using these responses it will be possible able to test different scenarios in the client, Service, and browser.

The Stub Verify Hub will provide the following responses:

| Response Scenario     | Message                                                                                                                   |
| --                    | --                                                                                                                         |
| Successful Match      | scenario: SUCCESS_MATCH, levelOfAssurance: ${specified loa}, pid: ${specified PID}                                         |
| Account Creation      | scenario: ACCOUNT_CREATION, levelOfAssurance: ${specified loa}, pid: ${specified PID}, attributes: [${user attributes}]  |
| No Match              | scenario: NO_MATCH                                                                                                         |
| Cancellation          | scenario: CANCELLATION                                                                                                     |
| Authentication failed | scenario: AUTHENTICATION_FAILED                                                                                           |
| Requestor Error       | scenario: REQUEST_ERROR                                                                                                 |
| Internal Server Error | scenario: INTERNAL_SERVER_ERROR
                      
Initially, the Stub Verify Hub will be deployed to Government PaaS.

A diagram of a potential stub architecture is available at: `prototypes/prototype-0/docs/diagrams/stub_service_architecture.png`

## Consequences

TBD
