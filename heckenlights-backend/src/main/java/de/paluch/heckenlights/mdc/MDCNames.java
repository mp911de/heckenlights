package de.paluch.heckenlights.mdc;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class MDCNames {

    /**
     * Log Correlation Id in MDC.
     */
    public static final String MDC_SESSION_ID = "Tracking.SessionId";

    /**
     * Log Correlation Id in MDC.
     */
    public static final String MDC_REQUEST_ID = "Tracking.RequestId";

    /**
     * Remote Address in MDC.
     */
    public static final String MDC_REMOTE_ADDR = "remoteAddr";

    /**
     * Remote User in MDC.
     */
    public static final String MDC_REMOTE_USER = "remoteUser";

    /**
     * Request URI in MDC.
     */
    public static final String MDC_REQUEST_URI = "requestUri";

    /**
     * Request Method in MDC.
     */
    public static final String MDC_REQUEST_METHOD = "requestMethod";

    /**
     * Profiling Start in MDC.
     */
    public static final String MDC_REQUEST_START = "profiling.requestStart";

    /**
     * Profiling End in MDC.
     */
    public static final String MDC_REQUEST_END = "profiling.requestEnd";

    /**
     * Profiling Duration in MDC.
     */
    public static final String MDC_REQUEST_DURATION = "profiling.requestDuration";

    /**
     * 
     */
    private MDCNames() {
    }
}
