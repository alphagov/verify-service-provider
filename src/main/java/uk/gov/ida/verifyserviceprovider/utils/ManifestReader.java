package uk.gov.ida.verifyserviceprovider.utils;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ManifestReader {
    private static final String MANIFEST_FILE_LOCATION = "/META-INF/MANIFEST.MF";
    private static final String UNKNOWN_VERSION = "UNKNOWN_VERSION";

    public String getVersion() {
        return getManifest().map(x -> x.getValue("Version")).orElse(UNKNOWN_VERSION);
    }

    private Optional<Attributes> getManifest() {
        Optional<String> manifestPath = getManifestFilePath();
        return manifestPath.map(this::getManifestAttributes);
    }

    private Attributes getManifestAttributes(String manifestPath) {
        try {
            return new Manifest(new URL(manifestPath).openStream()).getMainAttributes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<String> getManifestFilePath() {
        String simpleName = this.getClass().getSimpleName() + ".class";
        String pathToClass = this.getClass().getResource(simpleName).toString();
        String pathToJar = pathToClass.substring(0, pathToClass.lastIndexOf("!") + 1);
        return pathToJar.isEmpty() ? Optional.empty() : Optional.of(pathToJar + MANIFEST_FILE_LOCATION);
    }
}
