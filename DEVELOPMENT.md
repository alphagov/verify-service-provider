Development
=


If you want to make changes to `verify-service-provider` itself, fork the repository then:

__Test__
```
./pre-commit.sh
```

__Startup__
```
./startup.sh
```
You will need to either have environment variables set as above, or have edited the main configuration file (`verify-service-provider.yml`), or to pass an argument to this script for the application to start. Available arguments are `local-fed` for running against a locally running federation (see [verify local startup](https://github.com/alphagov/verify-local-startup)) or `vsp-only` for using default values to run against compliance tool on the reference environment.

__Build a distribution__
```
./gradlew distZip
```

You can find the distribution zip at `build/distributions`.

See [docs/development](https://github.com/alphagov/verify-service-provider/tree/master/docs/development) for more information about the development of the VSP, including how to run the application against a local compliance tool and see advanced configuration options.

