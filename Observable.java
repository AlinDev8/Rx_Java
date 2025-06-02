package customrx.core;

import customrx.schedulers.Scheduler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {
    private final OnSubscribe<T> onSubscribe;

    public interface OnSubscribe<T> {
        void subscribe(Emitter<T> emitter);
    }

    private Observable(OnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(OnSubscribe<T> onSubscribe) {
        return new Observable<>(onSubscribe);
    }

    public Disposable subscribe(Observer<T> observer) {
        AtomicBoolean disposed = new AtomicBoolean(false);

        Emitter<T> emitter = new Emitter<T>() {
            @Override
            public void onNext(T item) {
                if (!disposed.get()) observer.onNext(item);
            }

            @Override
            public void onError(Throwable t) {
                if (!disposed.get()) {
                    observer.onError(t);
                    disposed.set(true);
                }
            }

            @Override
            public void onComplete() {
                if (!disposed.get()) {
                    observer.onComplete();
                    disposed.set(true);
                }
            }
        };

        new Thread(() -> onSubscribe.subscribe(emitter)).start();

        return new Disposable() {
            @Override
            public void dispose() {
                disposed.set(true);
            }

            @Override
            public boolean isDisposed() {
                return disposed.get();
            }
        };
    }

    public <R> Observable<R> map(Function<T, R> mapper) {
        return create(emitter -> subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    emitter.onNext(mapper.apply(item));
                } catch (Throwable t) {
                    emitter.onError(t);
                }
            }

            @Override
            public void onError(Throwable t) {
                emitter.onError(t);
            }

            @Override
            public void onComplete() {
                emitter.onComplete();
            }
        }));
    }

    public Observable<T> filter(Predicate<T> predicate) {
        return create(emitter -> subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    if (predicate.test(item)) {
                        emitter.onNext(item);
                    }
                } catch (Throwable t) {
                    emitter.onError(t);
                }
            }

            @Override
            public void onError(Throwable t) {
                emitter.onError(t);
            }

            @Override
            public void onComplete() {
                emitter.onComplete();
            }
        }));
    }

    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return create(emitter -> subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                try {
                    mapper.apply(item).subscribe(new Observer<R>() {
                        @Override
                        public void onNext(R r) {
                            emitter.onNext(r);
                        }

                        @Override
                        public void onError(Throwable t) {
                            emitter.onError(t);
                        }

                        @Override
                        public void onComplete() {
                        }
                    });
                } catch (Throwable t) {
                    emitter.onError(t);
                }
            }

            @Override
            public void onError(Throwable t) {
                emitter.onError(t);
            }

            @Override
            public void onComplete() {
                emitter.onComplete();
            }
        }));
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return create(emitter -> scheduler.execute(() -> onSubscribe.subscribe(emitter)));
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return create(emitter -> subscribe(new Observer<T>() {
            @Override
            public void onNext(T item) {
                scheduler.execute(() -> emitter.onNext(item));
            }

            @Override
            public void onError(Throwable t) {
                scheduler.execute(() -> emitter.onError(t));
            }

            @Override
            public void onComplete() {
                scheduler.execute(emitter::onComplete);
            }
        }));
    }
}