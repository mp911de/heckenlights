package de.paluch.heckenlights.tracking;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class PersistentAttributeHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private Map<String, Object> attributes = new HashMap<String, Object>();

    public PersistentAttributeHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public Object getAttribute(String name) {
        Object o = super.getAttribute(name);
        if (o == null) {
            o = attributes.get(name);
        }
        return o;
    }

    @Override
    public void setAttribute(String name, Object o) {
        attributes.put(name, o);
        super.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
        super.removeAttribute(name);
    }
}
