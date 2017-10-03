package uk.gov.ida.verifyserviceprovider.saml.extensions;

import uk.gov.ida.saml.core.extensions.impl.StringValueSamlObjectImpl;

public class ApplicationVersionImpl extends StringValueSamlObjectImpl implements ApplicationVersion {

    public ApplicationVersionImpl() {
        super(ApplicationVersion.NAMESPACE_URI, ApplicationVersion.DEFAULT_ELEMENT_LOCAL_NAME, ApplicationVersion.NAMESPACE_PREFIX);
    }

    public ApplicationVersionImpl(String namespaceURI, String localName, String namespacePrefix) {
        super(namespaceURI, localName, namespacePrefix);
    }
}
