package de.paluch.heckenlights.tracking;

import de.paluch.heckenlights.mdc.AbstractMDCFilter;
import de.paluch.heckenlights.mdc.DDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class TrackingMDCFilter extends AbstractMDCFilter {

    public static final String MDC_USER_ID = "Tracking.User";
    public static final String MDC_SESSION_ID = "Tracking.SessionId";
    public static final String MDC_REQUEST_ID = "Tracking.RequestId";

    @Override
    protected String createLogCorrelationId(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        RequestTracking.Data data = WebTracking.initialOrForwarded(httpRequest);

        if (data.rootRequestId != null) {
            DDC.put(MDC_REQUEST_ID, data.rootRequestId);
            DDC.setRequestId(data.rootRequestId);
        }

        if (data.rootSessionId != null) {
            DDC.put(MDC_SESSION_ID, data.rootSessionId);
        }

        if (data.userName != null) {
            DDC.put(MDC_USER_ID, data.userName);
        }

        httpRequest.setAttribute(DDC.class.getName(), DDC.currentContext());
        httpRequest.setAttribute(RequestTracking.class.getName(), data);

        return data.rootRequestId;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        DDC.clear();
        super.doFilter(new PersistentAttributeHttpServletRequestWrapper((HttpServletRequest) request), response, chain);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HttpServletResponse getWrapper(HttpServletResponse httpResponse, String logCorrelationId) {
        PersistentHeaderHttpServletResponseWrapper wrapper = new PersistentHeaderHttpServletResponseWrapper(httpResponse);

        wrapper.addPersistentHeader(getHeaderFieldName(), logCorrelationId);
        return wrapper;

    }
}
