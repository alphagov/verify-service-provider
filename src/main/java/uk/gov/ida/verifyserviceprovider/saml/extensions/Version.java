package uk.gov.ida.verifyserviceprovider.saml.extensions;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml1.core.AttributeValue;
import uk.gov.ida.saml.core.IdaConstants;

import javax.xml.namespace.QName;

public interface Version extends AttributeValue {
    String DEFAULT_ELEMENT_LOCAL_NAME = "AttributeValue";
    String NAMESPACE_URI = SAMLConstants.SAML20_NS;
    String NAMESPACE_PREFIX = SAMLConstants.SAML20_PREFIX;
    QName DEFAULT_ELEMENT_NAME = new QName(NAMESPACE_URI, DEFAULT_ELEMENT_LOCAL_NAME, NAMESPACE_PREFIX);
    String TYPE_LOCAL_NAME = "VersionType";
    QName TYPE_NAME = new QName(IdaConstants.IDA_NS, TYPE_LOCAL_NAME, IdaConstants.IDA_PREFIX);

    ApplicationVersion getApplicationVersion();
    void setApplicationVersion(ApplicationVersion applicationVersion);
}
