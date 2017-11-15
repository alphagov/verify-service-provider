# Verify Service Provider: API reference

This is the API documentation for Verify Service Provider. With the API you can:
* generate a SAML authentication request (AuthnRequest) with JSON
* translate a SAML response into JSON

To access Verify Service Provider, you must [download your own version](https://github.com/alphagov/verify-service-provider/releases).

The Verify team provides a client library for Node.js with the passport.js framework. If you would like to request a different client library, contact [idasupport+onboarding@digital.cabinet-office.gov.uk](mailto:idasupport+onboarding@digital.cabinet-office.gov.uk)].

See also:
* [Installing and configuring Verify Service Provider](/README.md)
* [Tutorial: How to securely exchange information using Verify Service Provider](/docs/tutorial-using-verify-service-provider.md)

## Generate a SAML authentication request

Generate a SAML authentication request, known as an AuthnRequest to send to Verify Hub. You must specify the level of assurance required.

See the [Verify Service Provider tutorial](ADD LINK) for more information.

Path: `POST /generate-request`

Example call:
```
> POST /generate-request HTTP/1.1
> Content-Type: application/json
>
> { "levelOfAssurance": "LEVEL_2" }
```

HTTP response codes:

| Code | Description |
| --- | --- |
| 200 | Contains an object with a valid SAML Authentication request that can be consumed by the Verify service.|
| 400 | Bad request. No response available. |
| 422 | An error due to a JSON request in an invalid format (e.g. missing mandatory parameters). |
| 500 | An error due to an internal server error. |

## Translate SAML response

Consume a SAML response from Verify Hub and translate into JSON.

See the [Verify Service Provider tutorial](ADD LINK) for more information on how to handle the response.

Path: `POST /translate-response`

Example call:
```
NEED EXAMPLE
```

HTTP response codes:

| Code | Description |
| --- | --- |
| 200 | Contains the details of the SAML response, translated into JSON - see 'Handle response scenarios' (ADD LINK) for next steps. |
| 400 | An error due to a problem translating the Response. |
| 422 | An error due to a JSON request in an invalid format (e.g. missing mandatory parameters). |
| 500 | An error due to an internal server error. |

## Definitions

RequestGenerationBody:

| Name | Type | Definition |
| --- | --- | --- |
| levelOfAssurance | string | The minimum level of assurance required by the Relying Party Service. |
| entityId | string | The Entity Id for the service interacting with the Verify Service Provider. This is required if the Verify Service Provider is set up for multi-tenanted use, otherwise it is optional. The value, if provided, must be one of those listed in the configuration for the Verify Service Provider. |

RequestResponseBody:

| Name | Type | Definition |
| --- | --- | --- |
| samlRequest | string | SAML Authn Request as a base64 string. |
| requestId | string | A token that identifies the Authn Request. This is used later to verify that the Authn Request and SAML Response have passed through the same browser. |
| ssoLocation | string | The url for Verify Hub SSO. The entrypoint for SAML authentication flow. |

TranslateSamlResponseBody:

| Name | Type | Definition |
| --- | --- | --- |
| samlResponse | string | A SAML Response as a base64 string. |
| requestId | string | A token that was generated for the original SAML Authn Request. This is used to verify that the Authn Request and SAML Response have passed  through the same browser. |
| levelOfAssurance | string | The minimum level of assurance required by the Relying Party Service. |

TranslatedResponseBody:

| Name | Type | Definition |
| --- | --- | --- |
| scenario | --- | --- |
| pid | string | A unique identifier that can identify a user against an internal record. |
| levelOfAssurance | string | The minimum level of assurance required by the Relying Party Service. |

Level of assurance:

| Name | Definition | Enum |
| --- | --- | --- |
| RequiredLevelOfAssurance | The minimum level of assurance required by the Relying Party Service. | LEVEL_1, LEVEL_2 |
| ReceivedLevelOfAssurance | Level of assurance the user was authenticated with. | LEVEL_1, LEVEL_2 |

Scenario:

| Scenario | Description |
| --- | --- |
| SUCCESS_MATCH | The user was successfully matched by your matching service. Will include `pid` and `levelOfAssurance`. |
| ACCOUNT_CREATION | User was successfully verified, but has not yet been matched. Will include full list of attributes so you can create a new user account. |
| NO_MATCH | User successfully verified, but could not be matched. Used by services that do not want to create new user accounts. |
| CANCELLATION | User opted to cancel verification at some point in their journey. Response will be empty. |
| AUTHENTICATION_FAILED | User could not be identified by identity provider. |
| REQUEST_ERROR | Internal error. |

Attributes:

An optional object containing attributes. If user-account-creation is enabled in the Relying Party  Service, and no match was possible, these attributes are used to create a new account.

| Name | Type | Value | Verified |
| --- | --- | --- | --- |
| firstName | object | string | boolean |
| middleName | object | string | boolean |
| surname | object | string | boolean |
| dateOfBirth | object | string - Format yyyy-MM-dd | boolean |
| address | object | - | boolean |

Address:

An object describing the address fields of a user

| Name | Description | Type |
| --- | --- | --- |
| postCode | --- | string |
| internationalPostCode | --- | string |
| uprn | --- | string |
| fromDate | Format yyyy-MM-dd | string |
| toDate | Format yyyy-MM-dd | string |

