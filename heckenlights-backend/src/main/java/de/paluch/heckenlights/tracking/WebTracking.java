package de.paluch.heckenlights.tracking;

import de.paluch.heckenlights.mdc.HostLogCorrelationId;
import de.paluch.heckenlights.mdc.LogCorrelationId;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class WebTracking {

    public final static String DELEGATION_ROOT_SESSION_ID = "X-Tracking.SessionId";
    public final static String DELEGATION_ROOT_REQUEST_ID = "X-Tracking.RequestId";
    public final static String DELEGATION_ROOT_USER_ID = "X-Tracking.UserId";

    public static RequestTracking.Data initialOrForwarded(HttpServletRequest httpServletRequest) {

        String userId = getUserId(httpServletRequest);
        String requestId = getRequestId(httpServletRequest);
        String sessionId = getSessionId(httpServletRequest);
        RequestTracking.set(userId, sessionId, requestId);
        return RequestTracking.get();

    }

    private static String getUserId(HttpServletRequest httpServletRequest) {
        String value = httpServletRequest.getHeader(DELEGATION_ROOT_USER_ID);
        if (StringUtils.isEmpty(value)) {
            if (httpServletRequest.getUserPrincipal() != null) {
                value = httpServletRequest.getUserPrincipal().getName();
            }
        }
        return value;
    }

    private static String getRequestId(HttpServletRequest httpServletRequest) {
        String value = httpServletRequest.getHeader(DELEGATION_ROOT_REQUEST_ID);
        if (StringUtils.isEmpty(value)) {

            value = (String) httpServletRequest.getAttribute(LogCorrelationId.CORRELATION_ID);
            if (StringUtils.isEmpty(value)) {
                value = HostLogCorrelationId.nextCorrelationId();
            }
        }

        httpServletRequest.setAttribute(LogCorrelationId.CORRELATION_ID, value);
        return value;
    }

    private static String getSessionId(HttpServletRequest httpServletRequest) {
        String value = httpServletRequest.getHeader(DELEGATION_ROOT_SESSION_ID);
        if (StringUtils.isEmpty(value)) {
            HttpSession session = httpServletRequest.getSession(false);
            if (session != null) {
                value = session.getId();
            } else {
                value = "none";
            }
        }
        return value;
    }

}
