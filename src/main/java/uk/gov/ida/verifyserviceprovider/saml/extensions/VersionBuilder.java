package uk.gov.ida.verifyserviceprovider.saml.extensions;

import org.opensaml.saml.common.AbstractSAMLObjectBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VersionBuilder extends AbstractSAMLObjectBuilder<Version> {

    @Nonnull
    @Override
    public Version buildObject() {
        return buildObject(Version.DEFAULT_ELEMENT_NAME, Version.TYPE_NAME);
    }

    @Nonnull
    @Override
    public Version buildObject(@Nullable String namespaceURI, @Nonnull String localName, @Nullable String namespacePrefix) {
        return new VersionImpl(namespaceURI, localName, namespacePrefix);
    }
}
