# 24. We will follow this release process for verify service provider

Date: 2017-10-27

## Status

Accepted

## Context

The verify-service-provider will be used by number of relying parties (RPs).
Currently to distribute the artifact to RPs we upload them manually to some server which is not a secure way of doing it.

So we wanted to automate the release process for verify service provider and make it more secure by signing the artifact. 
This will make RPs confident that they are not running any compromised software in their infrastructure.

## Decision

We decided to follow the below release process for verify service provider:

* During development, maintain #NEXT release notes :
    - Update the release notes with any breaking changes / new feature / anything important that RPs should know about

* When we decide to release the following steps have to be followed :
    - Finalise the release notes
    - Decide appropriate release number
    - Include diff link in release notes
    - Commit, tag it with release number and push release notes to master
    - The release jenkins job should be triggered and if it passes, it should produce an artifact
    - Ask for approval (in Google / Slack)
    - Release approver should check the artifact to ensure its proper one and sign it using the certificate 
    - Approver should also upload the signed artifact to github releases
    - Jenkins job to check if release artifact has been signed. If not, should send email to appropriate people
    - If release artifact is proper it should send an email to the RP's regarding new VSP.

## Consequences

We will have more secure way to distribute verify service provider artifacts to the RPs.
And RPs can be confident that they are not running any compromised software  by verifying the signature 
and they can be up-to-date with new releases of verify service provider.

