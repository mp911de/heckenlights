package de.paluch.heckenlights.mdc;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class ThreadLocalValueAdapter<T>{

    private ThreadLocal<T> threadLocal;

    public ThreadLocalValueAdapter() {
        threadLocal = new ThreadLocal<T>();
    }

    public ThreadLocalValueAdapter(ThreadLocal<T> threadLocal) {
        this.threadLocal = threadLocal;
    }

    public T get() {
        return threadLocal.get();
    }

    public void set(T value) {
        threadLocal.set(value);
    }

    public void remove() {
        threadLocal.remove();
    }
}
