package uk.gov.ida.verifyserviceprovider.saml.extensions;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ApplicationVersionBuilder extends AbstractSAMLObjectBuilder<ApplicationVersion> {
    @Nonnull
    @Override
    public ApplicationVersion buildObject() {
        return buildObject(ApplicationVersion.DEFAULT_ELEMENT_NAME);
    }

    @Nonnull
    @Override
    public ApplicationVersion buildObject(@Nullable String namespaceURI, @Nonnull String localName, @Nullable String namespacePrefix) {
        return new ApplicationVersionImpl(namespaceURI, localName, namespacePrefix);
    }
}
