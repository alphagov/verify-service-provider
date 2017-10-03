package uk.gov.ida.verifyserviceprovider.saml.extensions;

import uk.gov.ida.saml.core.IdaConstants;
import uk.gov.ida.saml.core.extensions.StringValueSamlObject;

import javax.xml.namespace.QName;

public interface ApplicationVersion extends StringValueSamlObject {
    String DEFAULT_ELEMENT_LOCAL_NAME = "ApplicationVersion";
    String NAMESPACE_URI = IdaConstants.IDA_NS;
    String NAMESPACE_PREFIX = IdaConstants.IDA_PREFIX;
    QName DEFAULT_ELEMENT_NAME = new QName(NAMESPACE_URI, DEFAULT_ELEMENT_LOCAL_NAME, NAMESPACE_PREFIX);
}
