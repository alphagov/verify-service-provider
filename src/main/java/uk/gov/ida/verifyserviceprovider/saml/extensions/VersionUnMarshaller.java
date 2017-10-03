package uk.gov.ida.verifyserviceprovider.saml.extensions;

import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;

public class VersionUnMarshaller extends AbstractSAMLObjectUnmarshaller {

    protected void processChildElement(XMLObject parentObject, XMLObject childObject) throws UnmarshallingException {
        Version version = (Version) parentObject;
    }
}
