package uk.gov.ida.verifyserviceprovider.logging;

import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class AuthnRequestAttributesHelper {

    private static final Logger log = LoggerFactory.getLogger(AuthnRequestAttributesHelper.class);

    public interface AuthnRequestAttibuteNames {
        String REQUEST_ID = "requestId";
        String DESTINATION = "destination";
        String ISSUE_INSTANT = "issueInstant";
        String ISSUER = "issuer";
    }

    public static void logAuthnRequestAttributes(AuthnRequest authnRequest) {
        try {
            MDC.put(AuthnRequestAttibuteNames.REQUEST_ID, authnRequest.getID() != null ? authnRequest.getID() : "");
            MDC.put(AuthnRequestAttibuteNames.DESTINATION, authnRequest.getDestination() != null ? authnRequest.getDestination() : "");
            MDC.put(AuthnRequestAttibuteNames.ISSUE_INSTANT, authnRequest.getIssueInstant() != null ? authnRequest.getIssueInstant().toString() : "");
            MDC.put(AuthnRequestAttibuteNames.ISSUER, authnRequest.getIssuer() != null ? authnRequest.getIssuer().getValue() : "");
            log.info("AuthnRequest Attributes: ");
        } finally {
            MDC.remove(AuthnRequestAttibuteNames.REQUEST_ID);
            MDC.remove(AuthnRequestAttibuteNames.DESTINATION);
            MDC.remove(AuthnRequestAttibuteNames.ISSUE_INSTANT);
            MDC.remove(AuthnRequestAttibuteNames.ISSUER);
        }
    }
}
