package de.paluch.heckenlights.tracking;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RequestTracking {

    private final static ThreadLocal<Data> threadLocal = new ThreadLocal<Data>();

    /**
     * Store Request Tracking Data.
     * 
     * @param userName
     * @param rootSessionId
     * @param rootRequestId
     */
    public static Data set(String userName, String rootSessionId, String rootRequestId) {

        Data data = new Data(userName, rootSessionId, rootRequestId);
        threadLocal.set(data);
        return data;
    }

    /**
     * Store Request Tracking Data.
     * 
     * @param data
     */
    public static void set(Data data) {

        threadLocal.set(data);
    }

    /**
     * Get Request Tracking Data if present.
     * 
     * @return
     */
    public static Data get() {
        return threadLocal.get();
    }

    public static class Data {

        public final String userName;
        public final String rootSessionId;
        public final String rootRequestId;

        public Data(String userName, String rootSessionId, String rootRequestId) {
            this.userName = userName;
            this.rootSessionId = rootSessionId;
            this.rootRequestId = rootRequestId;
        }
    }
}
