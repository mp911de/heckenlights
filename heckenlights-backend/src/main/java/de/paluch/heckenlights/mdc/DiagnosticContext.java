package de.paluch.heckenlights.mdc;

import java.util.Set;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DiagnosticContext
{
    /**
     *
     * @return Set of Diagnostic Keys.
     */
    Set<String> getNames();

    /**
     * Set a diagnostic parameter.
     * @param key
     * @param value
     */
    void put(String key, Object value);

    /**
     * Clear the context.
     */
    void clear();

    /**
     * Re-apply the context. May be useful in cross-threading contexts.
     */
    void apply();

    /**
     *
     * @return the request Id.
     */
    String getRequestId();

    void setRequestId(String requestId);
}
