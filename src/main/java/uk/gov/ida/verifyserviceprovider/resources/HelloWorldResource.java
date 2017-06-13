package uk.gov.ida.verifyserviceprovider.resources;

import uk.gov.ida.verifyserviceprovider.VerifyServiceProviderConfiguration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/hello-world")
public class HelloWorldResource {

    VerifyServiceProviderConfiguration configuration;

    public HelloWorldResource(VerifyServiceProviderConfiguration configuration) {
        this.configuration = configuration;
    }

    @GET
    public String getHelloWorld() {
        return "Hello " + configuration.getHelloWorldValue();
    }
}
