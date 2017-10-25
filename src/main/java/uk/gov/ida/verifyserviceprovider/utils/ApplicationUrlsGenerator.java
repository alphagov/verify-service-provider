package uk.gov.ida.verifyserviceprovider.utils;

class ApplicationUrlsGenerator {

    public final static String APPLICATION_URL_TYPE = "application-url-type";
    public final static String ADMIN_URL_TYPE = "admin-url-type";
    public final static String HEALTHCHECK_URL_TYPE = "healthcheck-url-type";

    private String APPLICATION_URL_PATTERN = "Application HTTP%s URL  %s- http%s://localhost:%d%s";
    private String ADMIN_URL_PATTERN = "Admin HTTP%s URL        %s- http%s://localhost:%d%s?pretty=true";
    private String HEALTHCHECK_URL_PATTERN = "Healthcheck HTTP%s URL  %s- http%s://localhost:%d%shealthcheck?pretty=true";

    private final String urlType;
    private final boolean isHttps;
    private final int port;
    private final String contextPath;

    public ApplicationUrlsGenerator(String urlType, boolean isHttps, int port, String contextPath) {
        this.urlType = urlType;
        this.isHttps = isHttps;
        this.port = port;
        this.contextPath = prepare(contextPath);
    }

    private String prepare(String contextPath) {
        String result = contextPath;
        if (!result.endsWith("/")) {
            result = result.concat("/");
        }

        return result;
    }

    public String generate() {
        String httpsString = isHttps ? "s" : "";
        String additionalSpacing = isHttps ? "" : " ";

        String url = "";
        switch (urlType) {
            case ADMIN_URL_TYPE:
                url = String.format(ADMIN_URL_PATTERN, httpsString.toUpperCase(), additionalSpacing, httpsString, port, contextPath);
                break;
            case HEALTHCHECK_URL_TYPE:
                url = String.format(HEALTHCHECK_URL_PATTERN, httpsString.toUpperCase(), additionalSpacing, httpsString, port, contextPath);
                break;
            case APPLICATION_URL_TYPE:
                url = String.format(APPLICATION_URL_PATTERN, httpsString.toUpperCase(), additionalSpacing, httpsString, port, contextPath);
                break;
        }

        return url;
    }
}
