package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static uk.gov.ida.verifyserviceprovider.utils.DefaultObjectMapper.OBJECT_MAPPER;

public class PrivateKeyDeserializer extends JsonDeserializer<PrivateKey> {

    @Override
    public PrivateKey deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        jsonParser.setCodec(OBJECT_MAPPER);
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        try {
            return createPrivateKey(Base64.getDecoder().decode(node.asText()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private PrivateKey createPrivateKey(byte[] cert) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec keySpec = new PKCS8EncodedKeySpec(cert);
        KeyFactory keyFactory;

        keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}
