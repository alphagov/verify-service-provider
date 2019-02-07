package uk.gov.ida.verifyserviceprovider.factories.saml;

import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;

import java.util.Optional;

public class SignatureValidatorFactory {
    public Optional<SamlAssertionsSignatureValidator> getSignatureValidator(Optional<ExplicitKeySignatureTrustEngine> trustEngine) {
        return trustEngine
            .map(MetadataBackedSignatureValidator::withoutCertificateChainValidation)
            .map(SamlMessageSignatureValidator::new)
            .map(SamlAssertionsSignatureValidator::new);
    }
}
