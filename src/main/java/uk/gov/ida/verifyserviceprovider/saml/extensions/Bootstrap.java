package uk.gov.ida.verifyserviceprovider.saml.extensions;

import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSString;
import uk.gov.ida.saml.core.extensions.impl.StringValueSamlObjectImpl;

public class Bootstrap {
    public static void bootstrap() throws InitializationException {
        InitializationService.initialize();
        XMLObjectProviderRegistrySupport.deregisterObjectProvider(XSString.TYPE_NAME);
        XMLObjectProviderRegistrySupport.registerObjectProvider(Version.DEFAULT_ELEMENT_NAME, new VersionBuilder(), VersionImpl.MARSHALLER, VersionImpl.UNMARSHALLER);
        XMLObjectProviderRegistrySupport.registerObjectProvider(ApplicationVersion.DEFAULT_ELEMENT_NAME, new ApplicationVersionBuilder(),
                StringValueSamlObjectImpl.MARSHALLER, StringValueSamlObjectImpl.UNMARSHALLER);
    }
}
