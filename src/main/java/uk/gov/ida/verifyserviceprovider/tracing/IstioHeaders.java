package uk.gov.ida.verifyserviceprovider.tracing;

public interface IstioHeaders {
    String X_REQUEST_ID = "x-request-id";
    String X_B3_TRACEID = "x-b3-traceid";
    String X_B3_SPANID = "x-b3-spanid";
    String X_B3_PARENTSPANID = "x-b3-parentspanid";
    String X_B3_SAMPLED = "x-b3-sampled";
    String X_B3_FLAGS = "x-b3-flags";
    String X_OT_SPAN_CONTEXT = "x-ot-span-context";

    String[] ISTIO_HEADERS = {
            X_REQUEST_ID,
            X_B3_TRACEID,
            X_B3_SPANID,
            X_B3_PARENTSPANID,
            X_B3_SAMPLED,
            X_B3_FLAGS,
            X_OT_SPAN_CONTEXT
    };
}
