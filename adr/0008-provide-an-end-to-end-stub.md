# 8. Provide an End To End Stub

Date: 12/06/2017

## Status

Accepted

## Context

We wish to run regular user research and testing against the prototype Service Provider.

To support user research we need to provide a user journey that resembles a typical Verify journey.

The area we are most interested in is the interface between the Service and the Hub.

## Decision

We will create a Stub Verify Hub that will allow for end-to-end testing of the prototype.

It will not provide a SAML implementation.

It will expect a form submission on a web resource that mimics the behaviour of receiving an AuthnRequest.

If the form post is successful then a browser redirect will be issued to a page containing a series of possible response scenarios that can be selected.
Some scenarios may require additional user input such as a providing a pid value.

Each response will mimic the shape of a Hub SAML Response form that can be submitted back to the Service and SP prototype.

The details of each canned response will be encoded as JSON in the SAMLResponse parameter of the form.

The prototype will be able to understand each canned response and produce a specific response to the Service/client.

Using these responses it will be possible able to test different scenarios in the client, Service, and browser.

The Stub Verify Hub will provide the following responses:

| Response Scenario     | Message                                                                                                     |
| --                    | --                                                                                                          |
| Successful Match      | scenario: SUCCESS_MATCH, loa: ${specified loa}, pid: ${specified PID}                                       |
| Account Creation      | scenario: ACCOUNT_CREATION, loa: ${specified loa}, pid: ${specified PID}, attributes: [${user attributes}]  |
| No Match              | scenario: NO_MATCH                                                                                          |
| Cancellation          | scenario: CANCEL                                                                                            |
| Authentication failed | scenario: AUTH_FAILURE                                                                                      |
| Requestor Error       | scenario: REQUESTOR_ERROR                                                                                   |

Initially, the Stub Verify Hub will be deployed to Government PaaS.

A diagram of a potential stub architecture is available at: `prototypes/prototype-0/docs/diagrams/stub_service_architecture.png`

## Consequences

TBD
