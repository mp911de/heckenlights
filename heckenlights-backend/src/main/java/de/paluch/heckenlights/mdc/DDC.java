package de.paluch.heckenlights.mdc;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class DDC {

    private static ThreadLocalValueAdapter<DiagnosticContext> valueAdapter = new ThreadLocalValueAdapter<DiagnosticContext>();

    private DDC() {

    }

    /**
     * Put a value to the diagnostic context.
     * 
     * @param key
     * @param value
     */
    public static void put(String key, Object value) {
        getContext().put(key, value);
    }

    private static DiagnosticContext getContext() {

        DiagnosticContext context = valueAdapter.get();
        if (context == null) {
            context = new DefaultDiagnosticContext();
            valueAdapter.set(context);
        }
        return context;
    }

    /**
     * Creates/retrieves the current context.
     * 
     * @return DefaultDiagnosticContext
     */
    public static DiagnosticContext currentContext() {
        return getContext();
    }

    /**
     * Clear all values within the diagnostic context.
     */
    public static void clear() {
        getContext().clear();
        valueAdapter.remove();
    }

    public static String getRequestId() {
        return getContext().getRequestId();
    }
    public static void setRequestId(String requestId)
    {
        getContext().setRequestId(requestId);
    }
}
