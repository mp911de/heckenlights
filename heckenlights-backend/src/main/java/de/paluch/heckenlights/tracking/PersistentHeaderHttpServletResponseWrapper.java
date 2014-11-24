package de.paluch.heckenlights.tracking;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Wrapper for a HttpServletResponse with providing persistent headers even if response is reset.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class PersistentHeaderHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private final Map<String, String> persistentHeaders = new HashMap<String, String>();

    /**
     * @param delegate
     */
    public PersistentHeaderHttpServletResponseWrapper(HttpServletResponse delegate) {
        super(delegate);
    }

    /**
     * @see javax.servlet.ServletResponse#reset()
     */
    @Override
    public void reset() {
        super.reset();

        Set<Entry<String, String>> set = persistentHeaders.entrySet();
        for (Entry<String, String> entry : set) {
            super.setHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @param key
     * @param value
     */
    public void addPersistentHeader(String key, String value) {
        persistentHeaders.put(key, value);
        super.addHeader(key, value);
    }

}
