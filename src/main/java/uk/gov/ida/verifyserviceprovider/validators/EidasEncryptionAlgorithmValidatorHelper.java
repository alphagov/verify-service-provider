package uk.gov.ida.verifyserviceprovider.validators;

import com.google.common.collect.ImmutableSet;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;

public class EidasEncryptionAlgorithmValidatorHelper {
    public static EncryptionAlgorithmValidator anEidasEncryptionAlgorithmValidator() {
        return new EncryptionAlgorithmValidator(
                ImmutableSet.of(
                        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128_GCM,
                        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES192_GCM,
                        EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256_GCM
                ),
                ImmutableSet.of(
                        EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP,
                        EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP11
                )
        );
    }
}
