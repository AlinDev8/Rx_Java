package customrx.core;

public interface Disposable {
    void dispose();
    boolean isDisposed();
}