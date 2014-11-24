package de.paluch.heckenlights.mdc;

import org.slf4j.MDC;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class Slf4jMDCAdapter
{

    public void put(String key, Object value) {
        if (key == null || value == null) {
            return;
        }
        MDC.put(key, toString(value));
    }

    private String toString(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    public Object get(String key) {
        return MDC.get(key);
    }

    public void remove(String key) {
        MDC.remove(key);
    }

    public boolean isAvailable() {
        try {
            Class.forName("org.slf4j.MDC");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
