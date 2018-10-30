package uk.gov.ida.verifyserviceprovider.services;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.core.transformers.MatchingDatasetUnmarshaller;
import uk.gov.ida.verifyserviceprovider.domain.AssertionData;

import java.util.List;

public interface AssertionDataTranslator {
    AssertionData translate(List<Assertion> assertions);
}
