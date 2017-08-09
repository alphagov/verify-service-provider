package uk.gov.ida.verifyserviceprovider.factories.saml;

import org.joda.time.DateTime;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLRuntimeException;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.SignatureFactory;
import uk.gov.ida.verifyserviceprovider.dto.LevelOfAssurance;

import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.UUID;

import static uk.gov.ida.verifyserviceprovider.utils.Crypto.publicKeyFromPrivateKey;

public class AuthnRequestFactory {

    private final URI destination;
    private final String serviceEntityId;
    private final PrivateKey signingKey;

    public AuthnRequestFactory(URI destination, String serviceEntityId, PrivateKey signingKey) {
        this.destination = destination;
        this.serviceEntityId = serviceEntityId;
        this.signingKey = signingKey;
    }

    public AuthnRequest build(LevelOfAssurance levelOfAssurance) {
        AuthnRequest authnRequest = new AuthnRequestBuilder().buildObject();
        authnRequest.setID(String.format("_%s", UUID.randomUUID()));
        authnRequest.setIssueInstant(DateTime.now());
        authnRequest.setForceAuthn(false);
        authnRequest.setDestination(destination.toString());

        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setValue(serviceEntityId);
        authnRequest.setIssuer(issuer);

        authnRequest.setSignature(createSignature());

        try {
            XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(authnRequest).marshall(authnRequest);
            Signer.signObject(authnRequest.getSignature());
        } catch (SignatureException | MarshallingException e) {
            throw new SAMLRuntimeException("Unknown problem while signing SAML object", e);
        }

        return authnRequest;
    }

    private Signature createSignature() {
        KeyPair signingKeyPair = new KeyPair(publicKeyFromPrivateKey(signingKey), signingKey);
        IdaKeyStore keyStore = new IdaKeyStore(signingKeyPair, Collections.emptyList());
        IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        SignatureRSASHA256 signatureAlgorithm = new SignatureRSASHA256();
        DigestSHA256 digestAlgorithm = new DigestSHA256();
        SignatureFactory signatureFactory = new SignatureFactory(keyStoreCredentialRetriever, signatureAlgorithm, digestAlgorithm);
        return signatureFactory.createSignature();
    }
}
