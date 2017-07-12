package uk.gov.ida.verifyserviceprovider.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.security.cert.Certificate;

public class CertificateDeserializer extends JsonDeserializer<Certificate> {

    @Override
    public Certificate deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException {
        jsonParser.setCodec(new ObjectMapper());
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        return new X509CertificateFactory().createCertificate(node.asText());
    }
}
