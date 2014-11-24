package de.paluch.heckenlights.mdc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class DefaultDiagnosticContext implements DiagnosticContext {

    private Slf4jMDCAdapter mdcAdapter = new Slf4jMDCAdapter();

    private Map<String, Object> content = new HashMap<String, Object>();

    private String requestId;

    public DefaultDiagnosticContext() {
    }

    @Override
    public Set<String> getNames() {
        return content.keySet();
    }

    @Override
    public void put(String key, Object value) {
        if (key == null || value == null) {
            return;
        }
        content.put(key, value);
        mdcAdapter.put(key, value);
    }

    @Override
    public void clear() {
        for (String key : getNames()) {
            mdcAdapter.remove(key);
        }

        content.clear();
    }

    @Override
    public void apply() {
        for (Map.Entry<String, Object> entry : content.entrySet()) {
            mdcAdapter.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String getRequestId() {
        String requestId = this.requestId;
        if (requestId == null) {
            requestId = HostLogCorrelationId.nextCorrelationId();
            this.requestId = requestId;
        }

        return requestId;
    }

    @Override
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
