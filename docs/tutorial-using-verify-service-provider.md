# How to securely exchange information using Verify Service Provider

GOV.UK Verify uses SAML (Security Assertion Markup Language) to securely exchange information about identities. A
Relying Party can use [Verify Service Provider](https://github.com/alphagov/verify-service-provider/blob/master/README.md)
to generate SAML to send to the Verify Hub and translate the SAML responses returned by the Verify Hub.

This tutorial explains how to:
* generate a SAML (AuthnRequest) request with Verify Service Provider
* send the AuthnRequest to Verify Hub
* translate the SAML Response from Verify Hub into JSON
* address any errors
* handle the different response scenarios
* deal with returning users
* approach troubleshooting and get help

## Pre-requisites

To be able to follow this tutorial:
* you must [set up and configure Verify Service Provider](https://github.com/alphagov/verify-service-provider/blob/master/README.md)
* you must have a working Matching Service Adapter running(LINK TO MSA DOCS)
* your installation of Verify Service Provider must pass its health check(INFO NEEDED)

## Generate a SAML request (AuthnRequest)

When a user indicates they want to use Verify, your service must send a request to the Verify Hub to start the identity
verification process. That request must be in SAML and is known as an AuthnRequest.

You'll need to:
1. Generate an AuthnRequest
2. Securely store `requestId` from the response

### Step 1: Generate an AuthnRequest

You can create an AuthnRequest using the Verify Service Provider API.

You'll need to use the `/generate-request` endpoint and state the level of assurance you want to confirm for that user.

Example call:
```
> POST /generate-request HTTP/1.1
> Content-Type: application/json
>
> { "levelOfAssurance": "LEVEL_2" }
```

<details>
<summary>
Example response:
</summary>
```
< HTTP/1.1 200 OK
< Content-Type: application/json
< 
{
    "samlRequest": "LONG SAML",
    "requestId": "_f43aa274-9395-45dd-aaef-25f56f49084e",
    "ssoLocation": "https://compliance-tool-reference.ida.digital.cabinet-office.gov.uk/SAML2/SSO"
}
```
</details>

<br>The response contains:
* `samlRequest` - your base64 encoded AuthnRequest
* `requestId` - a token that identifies the AuthnRequest. Used to connect the user's browser with a specific request.
* `ssoLocation` - a URL for Verify Hub single sign on

**HTTP response codes**

Common [response codes](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes) include:

| Code | Description |
| --- | --- |
| 200 | Contains an object with a valid SAML Authentication request that can be consumed by the Verify service.|
| 400 | Bad request. |
| 422 | An error due to a JSON request in an invalid format (e.g. missing mandatory parameters). |
| 500 | An error due to an internal server error. |

### Step 2: Securely store `requestId` from the response

You will need to access the `requestId` later in the process to validate the user's browser came from a specific AuthnRequest.

You must securely store the `requestId` and save it to the user's session. We recommend you store the `requestId` in a secure cookie. If you store the requestId in a cookie you must ensure the cookie is signed to prevent tampering.

## Send an AuthnRequest to Verify Hub

Next, you need to send your newly-generated AuthnRequest to the Verify Hub. This is done through the user's browser. You can do this by submitting an HTML form via their browser, as per the [SAML HTTP Post Binding](https://en.wikipedia.org/wiki/SAML_2.0#HTTP_Post_Binding).

The HTML form should include:
* escape HTML - to make sure no symbols or special characters are processed
* auto-post - to automatically send the user onto the next stage of their Verify journey
* page styling to display if JavaScript is disabled - to prompt users to turn on JavaScript. This should look like your service

<details>
<summary>
Example [XHTML form from passport-verify](https://github.com/alphagov/passport-verify/blob/master/lib/saml-form.ts):
</summary>

```
    <form class='passport-verify-saml-form' method='post' action='${escape(ssoLocation)}'>
      <h1>Continue to next step</h1>
      <p>Because Javascript is not enabled on your browser, you must press the continue button</p>
      <input type='hidden' name='SAMLRequest' value='${escape(samlRequest)}'/>
      <input type='hidden' name='relayState' value=''/>
      <button class='passport-verify-button'>Continue</button>
    </form>
    <script>
      var form = document.forms[0]
      form.setAttribute('style', 'display: none;')
      window.setTimeout(function () { form.removeAttribute('style') }, 5000)
      form.submit()
    </script>
    <style type='text/css'>
      body {
        padding-top: 2em;
        padding-left: 2em;
      }
      .passport-verify-saml-form {
        font-family: Arial, sans-serif;
      }
      .passport-verify-button {
        background-color: #00823b;
        color: #fff;
        padding: 10px;
        font-size: 1em;
        line-height: 1.25;
        border: none;
        box-shadow: 0 2px 0 #003618;
        cursor: pointer;
      }
      .passport-verify-button:hover, .passport-verify-button:focus {
        background-color: #00692f;
      }
    </style>
```

</details>  

<br>If you are using the [Verify Compliance Tool](https://alphagov.github.io/rp-onboarding-tech-docs/pages/saml/samlComplianceTool.html#test-your-service-with-the-saml-compliance-tool), you can test you can send AuthnRequests successfully.

## Translate a SAML response into JSON

Once the Verify Hub has received an AuthnRequest, it will pass the information onto the Identity Provider (IdP). The IdP will check if it can verify the user. That information will be sent back to the Verify Hub and the Hub will send it to your Matching Service Adapter to see if there is any user matching the information in any database. That full response is then sent back to Verify Hub, then to the user's browser and back to your service.

The SAML response will be posted to the URL/endpoint you specified when setting up your Verify environments. For example, `passport-verify` uses `/verify/response`.

You can use the Verify Service Provider to translate the SAML Response into more readable JSON with `/translate-response`.

The call must contain:
* `samlResponse` - the raw SAML returned from the Verify Hub
* `requestId` - the token you stored in Step 2 from your AuthnRequest
* `levelOfAssurance` - to validate that the user meets the minimum level of assurance you have requested

<details>
<summary>
Example call:
</summary>
```
> POST /translate-response HTTP/1.1
> Content-Type: application/json
>
{
  "samlResponse" : "LONG SAML",
  "requestId" : "_64c90b35-154f-4e9f-a75b-3a58a6c55e8b",
  "levelOfAssurance" : "LEVEL_2"
}
```
</details>

Example successful match response:
```
< HTTP/1.1 200 OK
< Content-Type: application/json
< 
{
    "scenario": "SUCCESS_MATCH",
    "pid": "etikgj3ewowe",
    "levelOfAssurance": "LEVEL_2",
    "attributes": null
}
```

Example successful user account creation response:
```
< HTTP/1.1 200 OK
< Content-Type: application/json
< 
{
    "scenario": "ACCOUNT_CREATION",
    "pid": "etikgj3ewowe",
    "levelOfAssurance": "LEVEL_2",
    "attributes": {
        "firstName": {
            "value": "Screaming",
            "verified": true
        },
        "middleName": {
            "value": "Jay",
            "verified": false
        },
        "surname": {
            "value": "Hawkins",
            "verified": true
        },
        "dateOfBirth": {
            "value": "1977-07-21",
            "verified": false
        },
        "address": {
            "value": {
                "lines": [
                    "33 Example Street"
                ],
                "postCode": "WC1 7AA",
                "internationalPostCode": null,
                "uprn": null,
                "fromDate": null,
                "toDate": null
            },
            "verified": true
        },
        "addressHistory": [
            {
                "value": {
                    "lines": [
                        "33 Example Street"
                    ],
                    "postCode": "WC1 7AA",
                    "internationalPostCode": null,
                    "uprn": null,
                    "fromDate": "2016-11-01",
                    "toDate": null
                },
                "verified": true
            },
            {
                "value": {
                    "lines": [
                        "33 Old Street"
                    ],
                    "postCode": "WC1 6AA",
                    "internationalPostCode": null,
                    "uprn": null,
                    "fromDate": "2015-11-01",
                    "toDate": "2016-11-01"
                },
                "verified": false
            }
        ],
        "cycle3": "NINO"
    }
}
```

**HTTP response codes**

Common [response codes](https://en.wikipedia.org/wiki/List_of_HTTP_status_codes) include:

| Code | Description |
| --- | --- |
| 200 | Contains the details of the SAML response, translated into JSON - see 'Handle response scenarios' (ADD LINK) for next steps. |
| 400 | An error due to a problem translating the Response. |
| 422 | An error due to a JSON request in an invalid format (e.g. missing mandatory parameters). |
| 500 | An error due to an internal server error. |

## How to handle 200 responses

When you receive an HTTP 200 response, it will contain one of 6 SAML scenarios with:
* `pid` - a unique identifier that can identify a user against a record in your internal database(s)
* `levelOfAssurance` - the user's minimum level of assurance
* `attributes` - name, date of birth, and other information you can use to create a new user account, if required

| Scenario | Description |
| --- | --- |
| SUCCESS_MATCH | The user was successfully matched by your matching service. Will include `pid` and `levelOfAssurance`. |
| ACCOUNT_CREATION | User was successfully verified, but has not yet been matched. Will include full list of attributes so you can create a new user account. |
| NO_MATCH | User successfully verified, but could not be matched. Used by services that do not want to create new user accounts. |
| CANCELLATION | User opted to cancel verification at some point in their journey. Response will be empty. |
| AUTHENTICATION_FAILED | User could not be identified by identity provider. |
| REQUEST_ERROR | Internal error. Contact  idasupport+onboarding@digital.cabinet-office.gov.uk. |

## Returning users
If a user has previously verified their identity with an identity provider, the API should return a recognised `pid` or unique identifier for that user. Your Matching Service Adapter should follow the [matching cycle 0](http://alphagov.github.io/rp-onboarding-tech-docs/pages/ms/msWorks.html?highlight=matching%20cycle#ms-mc0) to find the `pid` in your database(s).

If you receive `ACCOUNT_CREATION` when you expect `SUCCESS_MATCH`, there may be an issue with your Matching Service Adapter.

## Support and troubleshooting

If you're having any issues generating, sending or translating SAML, you should follow these 3 steps in order until you resolve the issue:

1. Check your Verify Service Provider is setup properly. Use `GET /health-check` to confirm it is running correctly.
2. Test your setup with Verify Compliance Tool.
3. Contact the GOV.UK Verify team for helping decoding the SAML. 

### Contact us

If you have any questions about this guide or would like further support, raise a GitHub issue or contact idasupport+onboarding@digital.cabinet-office.gov.uk.

If you think you have discovered a security issue in this code please email disclosure@digital.cabinet-office.gov.uk with details.

