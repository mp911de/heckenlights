package de.paluch.heckenlights.mdc;

import static de.paluch.heckenlights.mdc.MDCNames.MDC_REMOTE_ADDR;
import static de.paluch.heckenlights.mdc.MDCNames.MDC_REMOTE_USER;
import static de.paluch.heckenlights.mdc.MDCNames.MDC_REQUEST_ID;
import static de.paluch.heckenlights.mdc.MDCNames.MDC_REQUEST_METHOD;
import static de.paluch.heckenlights.mdc.MDCNames.MDC_REQUEST_START;
import static de.paluch.heckenlights.mdc.MDCNames.MDC_REQUEST_URI;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * MDC Filter for adding a Message Diagnostics Context.
 * 
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public abstract class AbstractMDCFilter implements Filter {

    /**
     * Parameter-Name for HTTP Header Field (for Log-Correlation Id)
     */
    public static final String HEADER_FIELD_NAME_PARAM = "HeaderFieldName";

    private String headerFieldName = "X-CorrelationId";

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (filterConfig != null) {
            String value = filterConfig.getInitParameter(HEADER_FIELD_NAME_PARAM);
            if (value != null && !value.trim().equals("")) {
                headerFieldName = value;
            }
        }
    }

    protected String createLogCorrelationId(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String logCorrelationId = HostLogCorrelationId.nextCorrelationId();
        return logCorrelationId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        ServletResponse localResponse = response;
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            String logCorrelationId = createLogCorrelationId(httpRequest, httpResponse);
            request.setAttribute(MDC_REQUEST_ID, logCorrelationId);

            setupMDC(httpRequest, httpResponse, logCorrelationId);
            HttpServletResponse wrapper = getWrapper(httpResponse, logCorrelationId);

            localResponse = wrapper;
        }

        chain.doFilter(request, localResponse);
    }

    protected void setupMDC(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String logCorrelationId) {

        DDC.put(MDC_REQUEST_ID, logCorrelationId);
        DDC.put(MDC_REMOTE_ADDR, httpRequest.getRemoteAddr());

        if (httpRequest.getRemoteUser() != null) {
            DDC.put(MDC_REMOTE_USER, httpRequest.getRemoteUser());
        }
        DDC.put(MDC_REQUEST_URI, httpRequest.getRequestURI());
        DDC.put(MDC_REQUEST_METHOD, httpRequest.getMethod());

        DDC.put(MDC_REQUEST_START, "" + System.currentTimeMillis());
    }

    protected HttpServletResponse getWrapper(HttpServletResponse httpResponse, String logCorrelationId) {
        return httpResponse;
    }

    /**
     * @return the headerFieldName
     */
    public String getHeaderFieldName() {
        return headerFieldName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // noting to do.
    }

}
