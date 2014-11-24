package de.paluch.heckenlights.mdc;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class LogCorrelationId {

    /**
     * Constant for CorrelationId Logging (MDC).
     */
    public static final String CORRELATION_ID = "requestId";
    private static final double RANDON_MULTIPLIER = 1000000d;
    private static LogCorrelationId instance = new LogCorrelationId();

    private int correlationId;

    /**
     * Utility Constructor.
     */
    private LogCorrelationId() {
        correlationId = (int) (Math.random() * RANDON_MULTIPLIER);
    }

    /**
     * @return next CorrelationId
     */
    public static String nextCorrelationId() {

        synchronized (instance) {
            instance.correlationId++;
            return Integer.toString(instance.correlationId, Character.MAX_RADIX).toUpperCase();
        }
    }

}
