package uk.gov.ida.verifyserviceprovider.listeners;

import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.verifyserviceprovider.utils.HealthCheckTableFormatter;
import uk.gov.ida.verifyserviceprovider.utils.UsefulApplicationUrlsTableFormatter;

public class VerifyServiceProviderServerListener implements ServerLifecycleListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyServiceProviderServerListener.class);

    private final Environment environment;

    public VerifyServiceProviderServerListener(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void serverStarted(Server server) {
        LOGGER.info(UsefulApplicationUrlsTableFormatter.format(environment, server));
        LOGGER.info(HealthCheckTableFormatter.format(environment.healthChecks()));
    }
}