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

<details>
<summary>
Example call
</summary>
    
```
> POST /generate-request HTTP/1.1
> Content-Type: application/json
>
> { "levelOfAssurance": "LEVEL_2" }
```

</details>

<details>
<summary>
Example response
</summary>
    
```
{
    "samlRequest": "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOkF1dGhuUmVxdWVzdCB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIgRGVzdGluYXRpb249Imh0dHBzOi8vY29tcGxpYW5jZS10b29sLXJlZmVyZW5jZS5pZGEuZGlnaXRhbC5jYWJpbmV0LW9mZmljZS5nb3YudWsvU0FNTDIvU1NPIiBGb3JjZUF1dGhuPSJmYWxzZSIgSUQ9Il9mNDNhYTI3NC05Mzk1LTQ1ZGQtYWFlZi0yNWY1NmY0OTA4NGUiIElzc3VlSW5zdGFudD0iMjAxNy0xMC0zMVQxMDo0NDowMi4xOTJaIiBWZXJzaW9uPSIyLjAiPjxzYW1sMjpJc3N1ZXIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPmh0dHA6Ly92ZXJpZnktc2VydmljZS1wcm92aWRlci1sb2NhbDwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPgo8ZHM6U2lnbmVkSW5mbz4KPGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KPGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz4KPGRzOlJlZmVyZW5jZSBVUkk9IiNfZjQzYWEyNzQtOTM5NS00NWRkLWFhZWYtMjVmNTZmNDkwODRlIj4KPGRzOlRyYW5zZm9ybXM+CjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPgo8ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+CjwvZHM6VHJhbnNmb3Jtcz4KPGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3NoYTI1NiIvPgo8ZHM6RGlnZXN0VmFsdWU+VTRIU3NlK0ZRYTRKcWRQVm9wSU5GdExQOGkrcFFRSGdzdkxKOWNLb05JRT08L2RzOkRpZ2VzdFZhbHVlPgo8L2RzOlJlZmVyZW5jZT4KPC9kczpTaWduZWRJbmZvPgo8ZHM6U2lnbmF0dXJlVmFsdWU+CnIvd1JQdElvSUpGMWZRQTdBVFphamM0SDFVR3BDN0xweTFUdEkyUWNjR29IL3dGblA2dktYSld1U3E5dEJOcmZUd2xjT1BWVjB3dFoKMjZOVDdNTlowRWphZGNpcngyYXRpeExXc3B1WXk1TXI2aW5DelBvSVU3OTdYREJlMnZ3VVo3Tm9WSkpkWUwwcFVRWUxjdzdMWWZWYwpWQUhvWUxiL0RvZUFKVnhuTDh3dnhxd2k5dHJBME9UbTl5OXFZRk04STNWWk5Ma3JFYXYwZmY4VXVQNmZrKyszZTFLU3FVR0p6SW9SCjh6ZTBITjZWcmt4Y3Z1NUlyRTBUb0hlYlBzS0VoamNEbmcxS1lidnJpa1cyR090amR0R0JuTUdIQ2V0WnRXMWNJamRhcmxCZThvamUKWExnUmMwWklpUHVJbGNSRnN4a2JNZjhHdlZ2M0dUbWhkNDlob1E9PQo8L2RzOlNpZ25hdHVyZVZhbHVlPgo8L2RzOlNpZ25hdHVyZT48c2FtbDJwOkV4dGVuc2lvbnM+PHNhbWwyOkVuY3J5cHRlZEF0dHJpYnV0ZSB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiI+PHhlbmM6RW5jcnlwdGVkRGF0YSB4bWxuczp4ZW5jPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyMiIElkPSJfZDI4OTc0YjBmYjQ4ZTViYTE1OGRlZDU4MDQ4MzAzYmMiIFR5cGU9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI0VsZW1lbnQiPjx4ZW5jOkVuY3J5cHRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNhZXMxMjgtY2JjIi8+PGRzOktleUluZm8geG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPjxkczpSZXRyaWV2YWxNZXRob2QgVHlwZT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjRW5jcnlwdGVkS2V5IiBVUkk9IiNfMTI5ZDQ3ZTY0MGUwMGE4NWUzY2I3ZmJjYzU3YjNiYjIiLz48L2RzOktleUluZm8+PHhlbmM6Q2lwaGVyRGF0YT48eGVuYzpDaXBoZXJWYWx1ZT45WklNTlJMdWVyMXVCMU5lb3FqOEV3UmpBZW9oUmtSTmJRM3JPNHZpb1JNODRJN1VlSnJKZEFpeFRaanRnWXNJR3I5V0RaREVWOURkCitRSDR2ZjQ0b1hEQ1lMcFBSZncrclQ2S1ZuTTRqeVdkek52WUVUOER0N0JtOGdlUVhKdUVQK0EvVStOOU5YZzVaWmxqSm0rUjh2eUsKbWNTaWVpSllVS0hSdnlNRnZSVFcyL3VUTG4rampQWHZPVFdHaktpSlF5Q0V0UGoxeE11amhVVVJ1cFRIRHFwR2szN2JRQlRFODBKbwpEQmNKL2psSlpWNGI2SHVMN3IycEpqN0N4TFV2UlRJWDUzNVFBYXkxTU5HdXY2eWl5d2R5Zms2aDRZWDI5cXBudlhBVFJ2MzBhalVOCnJGWS8vYmRzRlpXT2NpZzZqNlgxcmRBOGJicFR2akc5YVpCRExuVGxDYVJldjY0b3U5MkdKbExWQm9hZE5SemM4cGM1QXllQVoxYUEKMHdSUU1HNmJMRFF0UWwwZkJZOEhBZHN5VGFaVEM4MGI4Z3Npa3hoRGRTVEo4djJDUkdTL00wODk0UndKR2FMaWo3c2c5c2tDbHVmWApScEFwSzhnU0d2dHh6MWVwMXdxbEhkYk9YQUdSOGQrR0tWTWRCTlVNSHdINzlLRmxHVjdtTmFPdGdDOXhxNkQ2K1lZNjU4U09sYVZuCnBnPT08L3hlbmM6Q2lwaGVyVmFsdWU+PC94ZW5jOkNpcGhlckRhdGE+PC94ZW5jOkVuY3J5cHRlZERhdGE+PHhlbmM6RW5jcnlwdGVkS2V5IHhtbG5zOnhlbmM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jIyIgSWQ9Il8xMjlkNDdlNjQwZTAwYTg1ZTNjYjdmYmNjNTdiM2JiMiI+PHhlbmM6RW5jcnlwdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3JzYS1vYWVwLW1nZjFwIj48ZHM6RGlnZXN0TWV0aG9kIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIiBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNzaGExIi8+PC94ZW5jOkVuY3J5cHRpb25NZXRob2Q+PHhlbmM6Q2lwaGVyRGF0YT48eGVuYzpDaXBoZXJWYWx1ZT5GTm94V21XZDI4dnZSdXFwMmRsQlVEVU8zOUVodzl4NnkvbGFlMWszQ1BTOGVIOWN5eFhZOHZUMUp0TUR1T3RCQnZhSUx2aTdTcDNCCmlObDdHLzkzV04zRUVNUU82UU1XbDlvVzBJdFMzWmlCTFFwT0tQbjVsbk0xV01tM2RIZVd6alVUcDNqQmRTWGVXbUdNSXhDbVl2N2YKOVNpbm55NExyMGtFWXJUMGUrUjZJblpjeGJiL0ViTmVUSThPT3ZxWjR5WldnUTJVcHUraE1CN1dsaTZYOWdBNlowalJGWUY0aTAxdgpIOWhnYWtBT1JmZGVDbVF6b1dQMlVpUEQwc3NEQ3RMS0FVT3lzWXNhbWl6cHVkakxMTW5PaGlJenQ1eXZmSmh6ckZFWnQ3Q2R0ZFliCkRQbEtUMUlwSDJNdlBWa2E4bmd3TE0zZG5GNU5WMk9PZVNRV2VnPT08L3hlbmM6Q2lwaGVyVmFsdWU+PC94ZW5jOkNpcGhlckRhdGE+PHhlbmM6UmVmZXJlbmNlTGlzdD48eGVuYzpEYXRhUmVmZXJlbmNlIFVSST0iI19kMjg5NzRiMGZiNDhlNWJhMTU4ZGVkNTgwNDgzMDNiYyIvPjwveGVuYzpSZWZlcmVuY2VMaXN0PjwveGVuYzpFbmNyeXB0ZWRLZXk+PC9zYW1sMjpFbmNyeXB0ZWRBdHRyaWJ1dGU+PC9zYW1sMnA6RXh0ZW5zaW9ucz48L3NhbWwycDpBdXRoblJlcXVlc3Q+",
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
Example <a href="https://github.com/alphagov/passport-verify/blob/master/lib/saml-form.ts">HTML form from passport-verify</a>
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

</details>

<details>
<summary>
Example call
</summary>

```
> POST /translate-response HTTP/1.1
> Content-Type: application/json
>
{
  "samlResponse" : "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48c2FtbDJwOlJlc3BvbnNlIHhtbG5zOnNhbWwycD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiBEZXN0aW5hdGlvbj0iaHR0cDovL2xvY2FsaG9zdDozMjAwL3ZlcmlmeS9yZXNwb25zZSIgSUQ9Il84MjE4NjcyOC01MjA4LTQ5ZDQtOWMzZi05ZjFmNTUxYzkzOWMiIEluUmVzcG9uc2VUbz0iXzY0YzkwYjM1LTE1NGYtNGU5Zi1hNzViLTNhNThhNmM1NWU4YiIgSXNzdWVJbnN0YW50PSIyMDE3LTEwLTMxVDExOjM2OjIyLjEwMloiIFZlcnNpb249IjIuMCIgeHNpOnR5cGU9InNhbWwycDpSZXNwb25zZVR5cGUiPjxzYW1sMjpJc3N1ZXIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5Ij5odHRwczovL3NpZ25pbi5zZXJ2aWNlLmdvdi51azwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPgo8ZHM6U2lnbmVkSW5mbz4KPGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KPGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZHNpZy1tb3JlI3JzYS1zaGEyNTYiLz4KPGRzOlJlZmVyZW5jZSBVUkk9IiNfODIxODY3MjgtNTIwOC00OWQ0LTljM2YtOWYxZjU1MWM5MzljIj4KPGRzOlRyYW5zZm9ybXM+CjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjZW52ZWxvcGVkLXNpZ25hdHVyZSIvPgo8ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+CjwvZHM6VHJhbnNmb3Jtcz4KPGRzOkRpZ2VzdE1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3NoYTI1NiIvPgo8ZHM6RGlnZXN0VmFsdWU+dlFXbWVvdjNvRkFLUFg2c0NlQTl6aHVQRU1BRWhtQ2lDa2RHdjc2SytOUT08L2RzOkRpZ2VzdFZhbHVlPgo8L2RzOlJlZmVyZW5jZT4KPC9kczpTaWduZWRJbmZvPgo8ZHM6U2lnbmF0dXJlVmFsdWU+ClRsM2UrZFZNeXBwb1pDSG1EVVBBc0VZZnBBbUVMdDJ3RXk3WVdWb3JwQ3RKTmowZytoTm9VVE5xU1dvMzJjdUd5WTN5ZVltQWRqQUcKbnJtQUtORGx2ZlY4cTlZb3I1YXQweFVuM2I4b1NNcnVXNlFQNlU3SlhkUEVQZTV4Z3NxVDFnY1R1UVp1cWhWNjMzZDAzaVNMMkpKbQpOTVJOMWNoYXgxcXc1N09SNzArOEtxRFBRbXUyazRHeTFRaTd6ekt4cUxhQXBHbDVaSEVrQ3p6MkVHbndxeVAwQ1lVMVYzVkg1RkhKCi9BQkVuRDZVSTkzZWVsQlFkWmNXUGNqUG1FSk52bFFsVlNSS1IyN2dDQzV1YWVubTB1SnMyK2MvMjZGb3ZCZEk3WGlNUWdjTEg2ZTIKblhXTGYrbW9PbnVTeXBwTkNZVjh4ZFR1U3Y3bUFlZGhlV2NMR2c9PQo8L2RzOlNpZ25hdHVyZVZhbHVlPgo8L2RzOlNpZ25hdHVyZT48c2FtbDJwOlN0YXR1cyB4c2k6dHlwZT0ic2FtbDJwOlN0YXR1c1R5cGUiPjxzYW1sMnA6U3RhdHVzQ29kZSBWYWx1ZT0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnN0YXR1czpTdWNjZXNzIiB4c2k6dHlwZT0ic2FtbDJwOlN0YXR1c0NvZGVUeXBlIi8+PC9zYW1sMnA6U3RhdHVzPjxzYW1sMjpFbmNyeXB0ZWRBc3NlcnRpb24geG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iPjx4ZW5jOkVuY3J5cHRlZERhdGEgeG1sbnM6eGVuYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjIiBJZD0iXzExYTU0ODNkZWU5YzllMDIyYTkzNTM4ZjM1NThhMjljIiBUeXBlPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNFbGVtZW50Ij48eGVuYzpFbmNyeXB0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjYWVzMTI4LWNiYyIvPjxkczpLZXlJbmZvIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48ZHM6UmV0cmlldmFsTWV0aG9kIFR5cGU9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI0VuY3J5cHRlZEtleSIgVVJJPSIjXzIxOTFjMzY2MjBlMzQ2ZjU0OTRkYjg4MzA4MzYxYTE3Ii8+PC9kczpLZXlJbmZvPjx4ZW5jOkNpcGhlckRhdGE+PHhlbmM6Q2lwaGVyVmFsdWU+S1lEZVRVak1vWVJPR0hKODFIa3lodWlKc3RkenlwM2IwWHBOalE2bU4xcFMvck9LbW9LZEpOWWJCQ21Jdm9jNUNKWHVQbkZsdWtaZApaTWZTRk9ETDRTT3BOOXlTaWNyKzR2TWRFc2ZIZTBabjN3SUZ5VXY0TUU3emlZU0VIYndvTUJLM3A0RXVCYmNoMDNGVGZ2c29PbmdwCjFHQ3l5ODIydkxkaDdoZmFyWHNkMVBUNUdwaUZ4QTlNZ2I4eGcrRy9OTE9TVGM3L1pFT01XY214bHo5OHE3ZVhSdmNBYzM0S2hqaSsKNXV0aWd5OUZpUDBBclIxWEprVHhzYlByVFJnRHFsT0JwT1E0ODRubWdwMWN3QlRVZUFZRkY4ZFJPTnh2Q1dvNm9IZWZ6aU9UY21vOApOWEs1a04waFgrNjNBRU5RSm95dkk3VWZZMjdJYXlaSHk3SmdNWG03S1JpeHNVY0NhVHcyRU8xNU00VUNkdy9OVnRlZnZYN0pvYXdaCnJlMXNFblVyUW0yOStyYzFSSmU4cHc5UFZWeDhybkRKTnZnaHNJbUJUUFU2bHNMZFd6TEFhMkRxRzRXbjVzNTdXYk1rSE80T0Zid0IKUWpSVHhSbkhPT3lkQldqUDlYd0o0REY3M0dRZzBzQks2S1N4R2ZrTy9QNXBOWlNNWGZLMkQ1VWc2V2VsUXJ2b1czUXhJTCtHem9negpwQmZIRUV0VVZYU01PeWQ1cXhCaDJpdUdUd05JUy9IQ2RmZzM4T3dNSVEyUmtEc0g4ZFpFSUJhN2dTWXlzTWFZSlNMb05BRGUxMUJhClhNekRZSzVRS3BZVnJmbVUvYlBCRVVzK2xmNFl2OENjV2ZpMEVnbW5CSkdUNGFrQ3I4cjlQcGZzZ2VCd3VQamp2NDdUSVhYQVhCZzcKdk1QdGs3bHgxelJoR2VtS2lsS1FDQ1FkQWZOQTh0WHBTekw5SmhRLzJJYWsxVVlWSXNwSEVkUmxGMFZrcDU5QVd6aGJCNy95VG83UApRU3JNQTB2alROVW1PUUxuaGwxTDJ2SmVmUk42TUxULzNvOGVDeTZwNEREUGRacU5WNENLT1plODFFa3U2cExnUXpyN1hYYWVxcERzCmprU0lwN3owTzJndThuRUJkdFNWT1ZvbHdkQWhpOEpxL3E0bFFaSWJsSlRzL3diTXpucDExd0d1YmE3YlhRWFBGNnhmdnM2YWRHOTcKTDlwcDNWZW02N3UxMEREZlUrTzlDSnNLbzQ4Z3JjQ3dRYjcrL28yZW54em92TFpGUmhxRVlqMVJyTlJWcWMwZGltTUhTdnk4ajZObApyeDBxUFg0UU5WZWpBeUk0YnlwbUJ1MW1JT3V0YmNSSkFDU3hqRFNDaVFXelRqYUtQcGJNQXNDYnZrTXVVVTFCQXNQOW9McTcwVmRpCnJDS0txRWNENXd6YjdkcjlDaU5qb0drVUU2a0Q2a0NuTEErUVgvK29WaHIwemJPQ0tPbjdFUTdadnBRd0lmWXFjWDlKRU9uU1EreXcKblAxYXl5aVkvNFVKL1hneWF5L0Z5RmZBa1V1OG5hZjFEeERSS0JVR2FRcjE0d1Vid3RQVDZLYVNkdEFZamprY2lDMVhURldGbTI0cApUbkxOQXRRVkFZUVZOc25zeXJFRzBaM002Nlp0SW0zUmJPaEZSMW1pUEp1ZnBOUHpmdFE1ZCt4WHUvaUQxNjVjbEg5S0hETk5EZFlTCkRGdE8vNVVYTktLVUh4U1QyMUZjVitjRWUyV3VaSldTakdIcmkvNVJYOXBMc2RheVdIZXhzMTR1b28vZWlDdFZMenB0Wkl2dkNsOEYKMnE1N2VuRlFGd09HS3AxZEliODU4Qlhwb05vM080amRsQWZidklaUnRUSWFHYVNVc2Z1TGNML01UWjYyT1ZlWUpjZWQ4dkdaWWYzYwpjUTVwMnNZVW1ob2VjWXAyWnhXWUg4VTJlVy9BaU5VdVBpV0dQMFZ0NGY4UWNFVkt1NitUOU1rTW8xZG9GRExWUlpoYW9waHRkdFowCldtNk1mUmdNakJ6enZrOVdKSk9DbWJvUHp2cU9TWVhidmZ1a1JvMG50WEQxVmUwZjNEWmFUUXFDcHMyanJMWGJ5ZzhaeDRTTEowWkMKSThKQzM1d1JzZmV5c0RoVDFrZ3RCSms0SGVFR3BrdDBNMnZOUE5HL0V4RG9rbEtMVElnb3RkRlZOSmIyNTcyTzNRRGUxSFhIK2ZFSgpEa3JQQnczMjZVVWplQnUzV203SFBoYWdZTWVMcFZVSDlEU1NUNDhEV25LVUY4Z3Z3WlhGdDVJQ0tLd0FTRDVPRmQzV1hBb3h4VmUzClhiZHNVS0thd1FVSzZxMEtGSVcxSjRXdHlnK2pwVjFWUGg0WDk1WDUveXRlZ1R6YmxhYm5rSWpuR0N4WDRUVzJXUGZYMjVNanBuSy8KdDdXeHJjNzhSVDBPUTU1MWM2WUJVdVRoMm5Md2paVERMaUVFVkYrRUwrUDFPdm94L0QyRkJmRUxGaGRJenQ5eFJVN0pSWW1FT2pIWgpxMkFnR2xTVHpORFZSVHI0dzJwYzZORXVTY2Z4RmZOaCtIaWxndXVvWGdTZGFjdWo5U1FNSVhPWjhHK3RwY1Q2S0RPd2l6VDZqekxNCmJqRUFGYzBiQTFYK21rSER5SnQwUVQ2blByaGdHZUFRbGozUUVVSWxRRkszRFFFYXhUcnVoNFlObHJkZVlGeWdPZlF5akV0cXVMNlAKQ0F5U2lrbE4zMkRXMlFrcEtuZHBKZ3ZqTmlMNDgwZWdJNk1xbDNUeFdjc240MXk4MHYyZWFWWmIwRHMrMTUrRzZsUDAyNHI3MCs1TQpGWVYxcnYzZjhHaFlTeEwxOWZ0ZDZQZGNFNHQ1YXFrNU9wQXNkOThrSGhnckhyb0doYlJwYVFzK2w3MndtNnVEdjhUWVFqTkcvNTlQCk0yaXo2dm5vcGZxeXd3MGxMdk5ya2V0RUFldWpkSUFKRnNTVE1DMWRGYmxackxPVXRVY1h6czlKK3p3Sit4Vkc3TURPeUpKOXBGSVQKdmRtWG9pNXg3SzA3QXB0aWhVaXMycXVKa1IwN2pKekE5SlE3VkhzVWw2RlFJeFc4cS9Pazd3ejNYTzArZjdPV0tLTFdESk1MSENxNApvalI0VXplaUpoL29sRXJ6cWk5WVVZV01BVG1jTVFzZjBUYjZQZ2MvYVQzL2pqV0dPUVc4WDlSM0pFWWFmbmlRakNVTTliRkNvZlFZCllSbEdCN0Vrd29UL0JOUmFRQzByVVY3Z3BQSGN1M01paWcrRWc5V3dGUDZpWHZOYnhGdi9DcEpXN3J0amdkTE9BUzFqaEVaUlFPb1MKU1pOVDBNT0JJZG9zdndwS0tFak5GN1RmNWI0T1lMUVpvemR0b2xTN1BuS3JRK3RmTzBHOGdTSThscHI1bmNLWXppQ0QvYWR2WnNGZwpsZGhLeDhmZHhxYzY1S0dHUExCTVlpT0dzanZPZWMvb05zeTZ1MlhobWx5UFlUMUtxQXA4WkNLcFhGOEZ5b1p2ckUrMS9CTUhsY1F6CkdYTHFrTm4rN3BxY1ZGUVJzYnFBUEVDZktNcHl6NnlDOHVsbGdvQndva0M3TFJtcGN3dHFLbEs1YWw1eGFENG14akYxK1dCbnJPaC8KVXJBblFRSzh2MktaWC80emM1Nm1Pd3JtbGhIL3ozbnVaeEMxZitSNlpHanNOdG41N3NaamlzVUhpUWFOSFhwTUY1V3dOUWF1cFdndQpPeGpuVEIyZHNJblVEM2Faem1YbVFXNFNveHFjUmFkMDlUU3ZKWDU0NjZtNFFOWFp4bWZTU0xLQnJGYVViMHdsUDZnS01BbVJlUkduClVyWTVNdHNQUllNYVd5VnQ1Ull6QTFRelh3NDJhTTNNQUMrNzAyUFBOdW92SWNCWlJuclRvdUMxRkF4Vi90aW9wdTF3b0RMRVdwSjgKTStLa1BVcVd5eTlpcDN3TkFmTnFrcXAxaldKV0VQUzBsQXIxMlBDalJrRzFQbGozeERCYzdwNThpRzRqSUt5MlNvdS9JaGRxeVIvegpDTS90cExvL01oQmtiQW8rRDJKM1VOSU1PMkdtY1NXeFZUMFBINzRDcDFpY1JSTXR5OTZCU2UvOU9RTEhMUnFvQTZaZG95OEtJeWZVCjloa3Zsd2ZpaXVJUElaaVdWY3VMMDJ1S28xZTZ1M2tZWmFBRlRqNnV3YWhlcDF4bHhkWDBqR1JRbDhjRzFJMGNiNHR5bzlWOUpKcnoKZTE5M25ra1BjTVBoRTFpdXhaZlMwVHZEN0ZVN054ZFNkS25nUHlDNG9GSVVuTFVGMkluUTF3UXk5dHhLWStvTlpLRHpMdFhYbzN5Vgo5UlQvSmNzNitMYnd4cTJSdjFCSk5LVHhCaDVyc3YzbWYyTVlvS2haMVZ3cVc1dlhFVktPZjkxTEU0ZmZzOTFFUTN0YlNNdEYvZ21BCm9lV0ZwTVVLYk9FcGNkeTU3UHFEZU5iYlplZ1hTUk5UZ2hFQ2o2ZFdpRGtUVjN3UWxobEVvUEUrNWVvQXZSVTdHd2dlRnJTLzh5eDIKM0pTZ1J5eUMwOUhobFJlSGNkZTBWalE4UWw1V0ZBZi9UdE5IYU4zNlhwSk83UUU3a3RMb1Z1dERZdDhzZFZRcVpLOEptMk5MUE4vNAo3L2dCUTcwaFEzbFVqZzFlcnZaTDAvZFNYRmhtYkplcXJORDBCMWlhVTk3L3FTUy9rZFJxUWd1bEpHSUYrZFVud2lmU0NhQU1YUFVqClUyb3E4WjZ6alFFRHVFc3ZjalpqVnFEekQrOS9qUjYrVDJ3YmUrdG9nOEVkR3duOTEyaUpsUElEcUFVd2VPdzdJVG05TVlxWHhFcE4KaUN1eVdWdE1RWjg3V3Naa0JUYzBqa3lXRFBNUUo3cU9EaHBZb3NsODQ3dzIxdFFKakFSOWJxb2Q2eHBmejcvUjhkQmd0Nk5qVEcwbwoyZ2lqelAreiswemNBMTAyUW9HSm94Y1VUS0JWc3lZUXl3RDZGOXBKRkhnR1hGZXRFV0UyMDV1SlY2VFRySTZHNHJLQk5sUHhtREdHCi9aV0VISG1RNk5yaTkwVUt3czZmTFFDbzdUaFh2dDBsSEJDbmpkekxTcDZnTWJrYmZQbXpoYitWMmgvSTRhL3F3QUNLOE5FSTV6MHoKU3I5bm9UM1hoOU14aS9QUnYyUk9ibFljYStzRTBLSXJzSG94MkRCd29iVVhPRkc1Q2tqN1R4SENmSlRSL2RCTzVkN3RIa0g0akNCQQpBRk5iT1UwdHdtRzMzbVl3U01KTTJkbWR1Q0JNSHRLb3F5VmhHNWloVm1QdGNhbkNETzNtME9lRXI4V2YvOFpYdmZFQThiWCtYbThRCm1rb0IrZHQrT1ZJQnZtNmdJUWhnWnJJYzBpNTNxUVkvRGdEQ2FMM0dBOGZpclVPa1J2YWtsS0RHYitCZmZZUXg4N05vL1hVc05LUHcKckZQSGM2UUNJekZBZjR3Ym5WSVFSMGs3NUtkNE83MC9SSXREM3BHcXdSekg1TnpKcUN0YTRkcG44RmZNa3FKVkt5SkZ5c2VaLzJJTwpxSEZOUmNiSkhWcWJCaHhnWWFnK3Y3M1ljb28zSEluSTNCaVo5SjRqL2FpQVlpWTVuV1VaWURxT1NMWG42L0hBaUFMZzM1bkJ5ZURVCjk0TlpjN0N2Um1GRkhrQWhhcWlkMm1abUZtRWVnaTN6Rmp5cVRHb0ZYMitYTFFxOXF4NGpCTEtTdDhiVkxvOWozQ3RKc2k5QXpoU2gKT1JQb25KbHJvRGZTdmZwS3NLbDRhdlBIZ2F4eFg4VkhFaWdlcTJyckZsMFd5dXhheTFmcXBLSWRUdDg0dXBtQU5ncVhydFN0ckRkTQpXR2JYUWdrK1J4VkIrMnc2Vmg4bW9xQ2wvUDF3S3pLemtYZmlwQkRDZmhiYlJZcHVvYnpxcGg1YStmVk8xWllIWkdIVys1QnVWZW1KCjBJdTg5MCtBWWp3ZUhwTjFpVitxeDE2elNJMXhnRTRMZmdDRDV6YUN0dmEvM0lZQUpZYTZKa2dJVmEwWHdTTVdMZmduMzhaQXNTb2UKcjgxYk41anNrMU9tUTd1Y1RIZXdUN1poUlZCQWZGMGRwV1dUeGtNaVdaM2ZDVXk4S2pzUmZJYU0vR0tpYXM0dW9hMEdtcDA3YUVvRAp1VW9BQWdhZ2h3OFpKdmNGU0s2TkFkS3JuTW41STd4c015OGVkRUR0Uks1MnNmY0trbHlUNkNhcjdHbnZleUR1SjNKWkkvbW5Kd3k3CkVwcGdBaG56cTRmb1p1WkNaQTFKTHhrZTY3Znl2SFBLYzJpTTRoaStuMEVJMDlsU3BBNFBpMnFEYmdWNUcvb1pUV1dtMVBta2pqb28KWGdKalVwYkJldHhURW5Gc25YVURyR3hQZWo4SjlwZlpveEt2cHVaaTVKTEVZZzB1bG9VZmwreWhUOEk0TDhLMHBtN0NRUjlObTFaOQo1UEFGUVNJSXNBQUNrc1dtakdGOENncUx4ZldNN3V3WkZHUEdRUXBRSzBPekxLMldycUZrWlBjcVIvN0J5MlZBRkFJMElLSEd5UjRvClMrZWRTTThabnJUN3JHa1JIcUNSZ0dFU0w4djJRN1dCYzIrbnN1ME9JQkxUMGRaTUF0VjB6eDVvUjgzRE1ucDdmNTBEa1AvQ3lDZGsKUTFaYUxLaFRnSHB6WStpZzR0b0J0YXF3ZWxJSjNHcFBDSFhGRGhnV01NK1dDaTZRWWxIZkgzZHM3MEUzbjkxNU9qWTVma09iVmxoNgo0Q3Z5V2R5cVlvSkxEOWJTMW9YVnlxci8yb0hlZ05hZ3JSSjNrOVZ4ZkZZTGZGWFBvTGJxUldUVlpYSjZPQ0MrS3Y4clZnem9PckcyCkZnQ1BJODZBWjBTM2R0ZWVXbUhON1hGYVl3ZnJWbjFCRklGVVZYeDB5Rk92WEcxYVVUN1Babm5vb05lYzhBQlozbThXdXE4YWp5SDYKRTdxWUo0NVVmVUJDTDV2Z1ZVV0c4VlJ1bUMwYnlhUlY1MFpPZmZ1ZjFhNlp2b1NZUGtqLy9RR043bTRzL0R6UXhXSzdMVnVxczA5ZApTWUUrWkI5cTV6aCs1R3h4MVRuRURmVk9oS0N3bWs4T0JFUVRmUmo4VXdwWmxubG1yVjNBQ1RoWVdhUkx4YVRBMlkvM0JERlVEU1VJClF6Z2lGRFZuQkMxUzVrZGw2d3FQait4N1hwbG44WU9FQ1RZQ3BBWXY2WktqeGtmQXNOam8yNHIwYWcvazhRNHl5ZnZLZ0ZHMXZrSW0KWW9GMkVla09MNWZzMmZHTlRnR1lHc0lZangvWk1uY1d1TWNOQU51Z2dRR0d5RFlUaFo3WUdvL2o4eGJsVXFsbUJjdGVTWkFSVm40ZQpyZE83S21JM0RNZFdBYzlkdGFmOEFhd25ZbjI1cllRdlJ6MUtZUUpKa083ZmM2RjB6Q0tBQWQ0Zm1EUTJRSGYweElFZEpGVFBsRzFJCmIvSWZFU09GbkFzVUVIQ3FGaHVsNVZ6T0Rvb25wSTBJQjNqa2s4Y3R1ZFVuOUlSTW5aUDQ3R3RQQithcmZmWWtWclFIS1BCRVEvTmIKUXBpb3pnK1owcC9DWS93MjlORy8rcEhsMU9INnV6QnZnR3NEa3NKZzFyM3pMVnJTSmxrVzNLb0ZhS3NTQ3gyQzNyR1ZQUytuQTNwaApXb3JNRysxRkluSTc0alRTOXhvZnE4ZWdFMEJwc3pNN1RsVm9VY0RxVWRrc3ZtLzRzTDd1UmdETWdmT1lRQ1J5WVpRY2ltbzJzd0lDCk9wZU4zaGU2aUJkL1RVVjV4aUVnSStHaEgxYnR3V05MVjFPTkZ1S0x0cDU2K056c2pOYkhOdnR2MTEzbzRXQk5mbWduS2c1TFlzS00KczdadFlIS1J0Y1pDZGl0OENidG83RnVLdVFNVDNoR2xMVmpDZ0FXU24wZGhTMmhna3J3KzFoTkwvQ1UyN1Bvakd6MWVGT0dybjhHYwpZUU9JUU1QdklrN2lKMXdGLzk1blFjQWxneTI1czVORDJydkFvQlVPeUhKamdLZ1pQRVJBSDc4ekJaWnE4OVdSK2h4U0hHSjd2ZGRECjZyWGFGQ3VTQk9EL0h2SE5BN3hCZm11QTdyWGdRdlB2a2p2NWdxaWMwcjIvSVk4N0l5eW5vN29FY3J4UnY3bUIrUGVUWjlXblN2NTUKV25TL0djVi8yTzdZSEl0RVN1WTQ0ajVxWUtlWS9SWmk3RjkvaWxRVnVabVExa25nNFBIaVo5VUVka3p5Q1N2RGRFMk90VFJSQU1lNAoyK0VXbXc2TFhjQUVENlM4V3Zrell3clNCTE55dHNNelBaQ3MwWWFBcEhjL0tMK0dHSGU1ZHVZTzU4SUpNR0U5d1o0b0NaV2ZkWHpSCmFOKzlTNGJWdjVFcjRYSUhCWEpCM08rLytneHI0MnBtdG9qckRkQVJKRG03V0VhTUkzMThhd3NJTDQyMUp5VmptS1kwWVdQODcxcTIKaFNKM2k3VEMwSzdzT2dTMGlvQTZGSEpyajZZa3FLY1FET2Rud0wrN0N1TjgwcDM0RzdsK2RWLyt6WlVoM0pFbXVEUnFuUjdUZUlGZQpoNWxMeEl4MjVrYWpBM0NyQ1U3Q2pIdzErV2RSTjMyRklTRXJ4MktFK0s1TXBCODNyQzcrdlVEdnc1OG5NZm5NdHhoUHZlU3p4UVpmCmxZTkZDaGlRT0puNDg1T2VMTnRxaUdBRWZGd2oyWndGbXFDZTRyZ0ZxYUZWODl1K2lrVEg1eEtpV1pXRm5JMmNRZ0x6MjRDZ29PVzUKdzF0Y3dNL1VRM3I4b1dweGZwdENxQU1YT2ZzbzBicmR1SzZib2sxaTRrekpMRFNOcXNrYmNZMEcrZm5HWFFzbTZya2hRT2JwNnFzVwowVVZCSGtqYVhSK3VlRmhlRG1QcHp3SjR2UGpudzdjVFBWalA3bWFFN2I3cTdhc0d4U1N3SEtEUjFWWURGcmpLWCtEVnAxUGZBMFAzCmRXZ3NqdXJTUUw1bjNTYnJwYjN2YmNIZUNSYjRoK3ZxS2dkK21UTVZ1elR5NTBRaVdZQVgrMzVOdks2RjFtTXpwbkNEV2FoekxWNTgKMGhWMHRXR2Zsd2dCTTh2dDZDbDRhanVLS2ZuWXhTNHdUd1RNelhsc1hxQTZMcXJlMldtVnlUdGU1bXJwaGhsMUNsMWVzZGZySkg5Kwp6dTl4bG5hYVhBenBYdEJwTERRdmZyN1hQOHRTVDg5SkpWWEJmN245T2pBMnlEc1JJZnZmUUhwNS83MTRVVlF0bWc2VVA2V2hYWmx6CktDM2p4VWhyRlhoWmNodTJpM3V1VVVqcXVFbWFabWJ1ZSs3NnZYWHRHL2JzWngzbVh3eVJRMDVmVTFuL3RpbHRGSEJ4NFFBVnNSTVMKSGcrcXlzVWE0NGVDSEs2MTcvZmxNdGU5MGRtcFhKM0NFTHpUazdWeVNjc1FGVFk3YWQ1NTdyeUxZMk9GK0ljdHlVMTJyNU1VdVE3YwowZnVMZnpvdGhEZG1JUVI4ZEJZbTVaS3NRQUYxZzJQbm1hYjBjbFFQNzIybVQrNCtaRldWdStJV3d3R25QK1ovaEZuRi9wQlhTdjNUCnM5TGhZQ0Y1clE4Uk9BSzdSN2ROMGJtamQrK1JtKy9ubCtQVGFDYVpSa1cvQjM4ZGIzTHVQR3M1TS9DVXJmQ242Uk5WOHdMazJ6ZmkKNnR4QmxLRWxwWlVpTy9NVXNVa2p2NE5SeDVmaXhEZFpJUnovU3d6L3VUSFRaZWtlZjhLRTJrY1BIenliTXVIUjZ2dUJHb0thMHppVApRMjVBNWh2amY1YUNScU9PdHRqc25HSGN1dDZwR1ZvOXpLSVdSUk9CeU5GN1BmN0VibEhxOTcwZTFkTlRwYjByYm84WDJVUXpKU2JECktZWldoKzlpSFBDdHRnT1VhMnc5Sk9VSFIwK1RlM09Memc5eDg4MkpDTTdTYURPdUF3NTZmRHJsa1JRTDlZMjhVVkFBZVBKQ0ZBQnUKUnpST1dqc0JRN0tzdzhkSjkwcjhRSnozWkd0QW91Q3R4c0tTK00wNjNrOVRFWUN6aHZtVXc3cCs3Y2ZGMG1vcFJRcW0ra25vdDZjdQplSnpsS3d3em1pd3NVQzZ3WGVyY1NkUFhqdStaV1BEVThjK25tMmFTWDV6WGRXVWdKUGNyZUpqM25sT3cyL25ZZkpXK3lJcVVNSW1BCjZZNGdRYmNBRUFmaFJyYWVvVlRyd3RlSXQzZnlyUG44U2V1Rm9sdDBlVldXMUlqeUFGRXFnL3BTTFI4aS91YWg3UldVRHVOUW1ZL1cKRHRXRS9senVxMnJtL01pYldNSEZGR2dNY0l6b1hJU2VuVmtmNDNCYWpsemMvMERscW5Makl0dis0czZRYkxmSmcva0xhQ1MrckNOWgo2dXJQcmZheHh0MGM0SmcyWTNQeHJ1RVdBbmhUcEF2OU1RZlI0dVB4Rm9UeGNqVjd2N2xucEZXU0VjdEFtbWdNcGhKUXVNTVllcnQ4CkwwVU1rL1ZqWWNvRG5KSHFCQ1VDYmxQeGpmcDhLWEExbzFyaXZ6SndmS0tqWVFyamdlNlF3c0gzZG5yLzdrOHh2ZlJaUXpVWEcvamwKaUxGUUtVaWUybWFJOXoyR1cvNkVLVXc4VS9XTWZEZFZ3THFnMkpjUTRGZ2xlcjNCTGE2OXJaRGY5cG0rT2Jlb2NTQzh4QUJQZVRrZgpoR2ZzZVJIREFoUzBSeFZqd2VwZmxCV29PQnBPRWhNU3Q2SGhFK2VLcWYzb1F2MU82eVZJUys1eTlGdThOa2tMa2FudURhcUNxYm1LCjlzSHJZSW8ydDl3RitaR0NaYzRaMy83RXl6ZHJwZCsyUFJsMmErVi9ZRms4RlA1RzUrYThhcTJ1UVhqeGhSQ2NmbTRDUExFMHhnSnMKOTk4d205RlVBN2NVRklIU3VjVnZxcjliWk9xd3FIN2w0ZmZsb1VXb0xjNlpGZktxaUV6b0VSR2dxUStaMGZHZGRUZ0RhSGlWVUZBQgovdE1GYi9YZmswNlpSZWtKVnRiTmxFR0xRTWtuTVNiMVF0dEtKY3lBSWhGYzBwMWpWeUw1L2VrVUlocVhvS1VxenpWRmdOOFh0TFFlClpnVEhnS3lCVVlmTmV1U3JTOXg3bkZ0ZGlFcVQzVkdHaGJ5WVV3RnJENTd5R3U5d0NUL3dFUzlnaFI3SWtjbDh5cjF3Y1FGdXAyL1IKVGRpOFlubDJRaUlJL3ltSGR5UWM4K21HM3pBQXhhSE5NK3AzWElzRnV3UUZ0d2xPZ3ZkbVUwMERZTGpRMUJkOWZlOWtCeDM4Ym45ZgorZlZ3K1BPQlB3VVJoWUYrajkzK1h2eXl5bVR5WHZoMTlwZkhNK3FoeUwyM1BQek1zZkN3NkZ6WTUyRitqblBmOHYzTGhRNUk1VXMvCmNVUHN2SnFMblU5b25oUHhOdmZMOXN3aU92VFViZUVQWVZPRURzOXZ0MmpQRVVaV21xVEptMk1weHowN1VCbDFES3lEbUt5Z2hCdDIKdHRkeU5YeWo3UURlamlyVmp3Yko0ekgwU08wL3plWm42cmlDRENlank1NmFWNkpZODJzb2ZwczE3OExEbytYd2pNOEVoOEdORVMxZApaMmhFVkx1ejRpYWJMSDRMaHJHb2ROVlgrcFVXM2p1QlhRYkROdmt6WGREeTRDUFM3SnJpY1Fub2ZFVHRyZmc0cGxKMDBOVnFmZGhoCjVEYVBrMlRvR1JvZmN4L2Ivd1N4SG5PTjNDaWtFNmNKZUt3TFpOR3RDTG9qTHFCb1Y3OE1pM0lXbkRma29EV1UxWk1uQTM3dnovYkUKbnY0VFJuWm51d05XQXIzd09RMDJ3ZGhhVXBPUWRVWjNmVndNdkl4ZjJ1dWI5Wk53bjQrb3J1ajV2Tk9UbklMRk1tUE9XbURQaG5hcApJajZUYjBXcFh2OEJxelNpcVhOclFHd0x6aXpmY29WanVzTkI3RFRRYVBKK3Mxblcya1ljUVUvNk9YTjRiRGp5M2JuUHFZbFp4Y0V3Clgvd3RTWVphMjNrUm4vT2gxUy9zbGIwZVlWSGpaOFJFWjZnd2F0VDNjWHRmUjY2bGk3clpuazNVYmE4R1VUelFLb1FjT01hNGgra2QKeWU4c29aVG5JQ016MGRpYTRkcmNpTFAwVXlXbDRoTUo5a1N6V29iYVZ4dlZiYjlFN0p0S0xiS2gvOGJUeHJCcXJkMmN6bmxDNVlBVQpsREtWZ3IxUFdaU2VVcXZHSHBDclFuMXVuYVpzbGZTb0VVZDN4cFR6bWVOVm1xeEpua3lRS3F0LzdPYyt0OEt1aEV3cWlnMEJSMzRUCi9reXY2dldRck9Kc05pVkRnaEJ5SVYvWjM4UmNkN2hVL2pZK0JMN2Z2amJPa1hucTM1bE9BOWtOVUZWelQxSGxPYk11c24wc3ZkWWMKaHBsMlhOSmtiK1NrRWVWd0dkTnNUV1lCaTlYanZ2TWV4dEdjTjVIU1cwTUdPWVpkNkUydGhjZUxhclZOc240TnlpbzUxT3IwL0wyNApOeGpxamo2a1ZKRTdKSWtHYVgxZ1JBZ250djJIRUhMQ3FUWFFmRGRoclhSYWJiOE9mUklxQ3MvQWFjamJRczQwTkJaajk2UHlLVFJOCkhTWkpmTm44clV1VzFpV0dRRXJWV3JtOWdoVTJoQlJ0cCs2QUJtaXU2QWY5V05Qbk5acVZPblFRT0h1RzBEK3Y4VHV0N2I1QTg3c2cKcGNVRDl1WDA0SmFxcC9iOTFBMUR4RXZMM1VLbVI3Q2dxTTh4SkhNclU2aWhpcWlLdUJjNVREMFA0ZXI4RWEzV1lKbW5zZnVpK3lUWAoyVDNwWnA0RHZuMzhKWWQ4MVU2RVJwbHNBSmExSDNsN0ZjZlJTK0JjZmVQZ3RXSDFiVjJyMXNxUUJWcXBZZkF1ZVlEdEpVTDNDMWpECnZLS3NxS2t3cklXNGExSzZFYjVqdlkyWjlod1RQbXdKUW9NdXMxQWhIVFhIYXhURk1vSlZlTW9DUm53bzJvQlB1WTF2UWo4YW1sT3cKSHJKK0k2UnVPYjdSODJuZTJNRHRuZTg1cXJMSWhxWnRLRGo2a2NFSk1jLytnYXA3WDMrUzBOQi9VSDBtOHlQeDZocVEza2Z5cVhUTApLUWdzWkFXYmVENU5LbDFNOHZPSHBDRGhKMGpDVkZGRDF6aEVFelVFbUN1UWlIYWNwaFdPK0lnMzBuNnVzYisyVXltcldheVBZR1ZSClpmSTlwK1FNWHhrVWxhWGxCY1RndXRhbGpEcU0vZ25TNGtjMGJvVW5SN2M2Y3p1ZDNZNmczdlQyTzIrWDlpSWtoMUpyMFlzYjhQUkwKaHdtMThsRGFpWWN2UnByWnF2RXU0VlFFVFY5VWg4dkd1TFNnK1h0YlpDRUM4c1BCenRPanE1V2tPSnh3K2ZjZ0dxaFlxZWIrRjhJWQp3eC8rTG80YThGMGlBSDljTUR3TU15RVgzL2dDZ2U0MlFFR2ViOUdXTVpyeHV0OGJSWDdleUcyZncxOWM5QmZUYjdBTGYzUTMxWjZQCkE0TFpBeU1PMW5ucndQNGxQVTdMSkhCOFN4c2YxSzhWbUNDa1kvTlpiQlZieGdISVFrVHBsQk1FRlE3TzUwbUVpM2lzK2E3alNMOWYKOTBIc2FuR0Z1QktvT2VnZkFieUFrbkw3WndkZDZCTmdiSVhITExJRmFIdzFJRWs1ck5zeU05QndrR0x0MUdkY3l0WHdKQmoyUXY0OApkM3Rvd2hzMGlWQ21sYTZvMjJxMjdsUzV6enR2RWdwUnJUS09JK0pVemRubUxFTEdyb0IwWlkyb2ozbmNvOHluZXpPZm5uTlQ2RkZlCmdjOGtIbnFrOUJldy9sUE1kbklCTWEzZGliUmJvaktwY2xqZGRVb3NoWmFzL1ZaL0xhUFRkUUdMTnN2OWFtVT08L3hlbmM6Q2lwaGVyVmFsdWU+PC94ZW5jOkNpcGhlckRhdGE+PC94ZW5jOkVuY3J5cHRlZERhdGE+PHhlbmM6RW5jcnlwdGVkS2V5IHhtbG5zOnhlbmM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jIyIgSWQ9Il8yMTkxYzM2NjIwZTM0NmY1NDk0ZGI4ODMwODM2MWExNyI+PHhlbmM6RW5jcnlwdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jI3JzYS1vYWVwLW1nZjFwIj48ZHM6RGlnZXN0TWV0aG9kIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIiBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNzaGExIi8+PC94ZW5jOkVuY3J5cHRpb25NZXRob2Q+PHhlbmM6Q2lwaGVyRGF0YT48eGVuYzpDaXBoZXJWYWx1ZT5aQmpJTzNIc3kxSXZTZlJ3KytlRFVNVTBvWmtCSVFsbVI2dWZ6YnpLaUNrdDQ4SzMrdnRYcnh3eWp6ekczYzlISEFwNG91S290bjNQCnYrbU5NbkU3L3orZXVmd1NnUmJwWnBDU1pZTEFQMFNRdFZ6NmZTV3pDTWsxUGNXNyttdUhPTk1nRkpVcjhSaGVaTzZyUDI5RHBrRDUKckE4eHpDeTM2QXozSUVzRStDRmhEQzVhYmRqOWhFZ1NnTGZBTXp0OFdaUGNNcnppM3FYNGdXL0VWZVJmQnFyVEx5WEV4RkFiTjdJOQpmcjAwaHFsblBPY2NJTHBYaWZkMjZNbE03Y2FqSjNsdG5KOU13ZzZsZ0JFcEZ0dDNCSGliOW9KblB3QXQ1ZWtmUTlLL1BTTjFBcVZ3ClJ2UFoxZ0kzOHp1RzNnVGtuUWRxdS81Y1l0YkRKOXprNlNjTmNnPT08L3hlbmM6Q2lwaGVyVmFsdWU+PC94ZW5jOkNpcGhlckRhdGE+PHhlbmM6UmVmZXJlbmNlTGlzdD48eGVuYzpEYXRhUmVmZXJlbmNlIFVSST0iI18xMWE1NDgzZGVlOWM5ZTAyMmE5MzUzOGYzNTU4YTI5YyIvPjwveGVuYzpSZWZlcmVuY2VMaXN0PjwveGVuYzpFbmNyeXB0ZWRLZXk+PC9zYW1sMjpFbmNyeXB0ZWRBc3NlcnRpb24+PC9zYW1sMnA6UmVzcG9uc2U+",
  "requestId" : "_64c90b35-154f-4e9f-a75b-3a58a6c55e8b",
  "levelOfAssurance" : "LEVEL_2"
}
```

</details>

<details>
<summary>
Example successful match response
</summary>

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

</details>

<details>
<summary>
Example successful user account creation response
</summary>

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

</details>

<br>

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
