package uk.gov.ida.verifyserviceprovider.factories.saml;

import org.opensaml.xmlsec.signature.support.impl.ExplicitKeySignatureTrustEngine;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public class SignatureValidatorFactory {

    public SamlAssertionsSignatureValidator getSignatureValidator(@NotNull ExplicitKeySignatureTrustEngine trustEngine) {
        return Optional.of(trustEngine)
            .map(MetadataBackedSignatureValidator::withoutCertificateChainValidation)
            .map(SamlMessageSignatureValidator::new)
            .map(SamlAssertionsSignatureValidator::new)
            .get();
    }
}
