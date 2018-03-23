# 25. We will only release one configuration file

Date: 2017-11-06

## Status

Accepted

## Context

Historically we have had "two ways" of configuring Verify Service Provider:

- Using environment variables
- Using a YAML file

When using environment variables the application used the verify-service-provider-env.yml
file from the resources directory (so inside the jar). When using the YAML file you would
pass the path to a different file as a command line parameter - usually people
would use the example one that's contained in the repo.

There were a couple of reasons for the extra complexity of managing two files, both due to
restrictions with the java buildpack used by cloudfoundry:

- It's not possible to specify command line arguments through the java buildpack,
  so you can't specify a path to your config file
- We weren't confident in the way cloudfoundry manages static files, so we didn't want
  to rely on one.

There was also a philosophical point that 12 factor applications should be configured through
their environment. This made the "hide the configuration in the .jar and do everything through
env vars" way appealing.

## Decision

We will remove the verify-service-provider-env.yml file from src/main/resources

The application will default to the verify-service-provider.yml
file that's included in the .zip if no command line arguments are provided.

If the application is started without command line arguments specifying a yml file
AND no environment variables have been set, startup should error gracefully and tell
the user that the configuration fields have not been specified for example:

"ERROR - no configuration fields found, either set environment variables or specify
a configuration file using command line arguments ```server <path/to/verify-service-provider.yml>```"

We will establish the path to verify-service-provider.yml by asking java for the
path to the .jar file containing the Application class and looking in the parent
folder.

## Consequences

We will have to play a story to make the default configuration file work in a
way that's compatible with the current environment variable based solution.

Going forward, we will only need to maintain one configuration file instead of two.

Users will not have to learn about the dichotomy between "configure with env vars"
and "configure with files".

The application will still run on PaaS using the default java buildpack.

