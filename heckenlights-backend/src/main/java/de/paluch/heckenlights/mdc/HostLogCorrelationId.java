package de.paluch.heckenlights.mdc;

import biz.paluch.logging.RuntimeContainer;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class HostLogCorrelationId {

    /**
     * Create a correlation id with hostname.
     * 
     * @return HOSTNAME.correlationId
     */
    public static String nextCorrelationId() {
        return RuntimeContainer.HOSTNAME + "." + LogCorrelationId.nextCorrelationId();
    }

    private HostLogCorrelationId() {

    }
}
