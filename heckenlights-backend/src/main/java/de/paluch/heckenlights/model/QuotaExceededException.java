package de.paluch.heckenlights.model;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 01.12.13 20:46
 */
public class QuotaExceededException extends Exception {
    public QuotaExceededException(String message) {
        super(message);
    }
}
