package customrx;

import customrx.core.Observable;
import customrx.core.Observer;
import customrx.schedulers.IOThreadScheduler;
import customrx.schedulers.SingleThreadScheduler;

public class Main {
    public static void main(String[] args) {
        Observable.<Integer>create(emitter -> {
                    emitter.onNext(10);
                    emitter.onNext(20);
                    emitter.onNext(30);
                    emitter.onComplete();
                })
                .map(i -> i * 2)
                .filter(i -> i > 20)
                .subscribeOn(new IOThreadScheduler())
                .observeOn(new SingleThreadScheduler())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        System.out.println("Received: " + item + " on thread: " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable t) {
                        System.err.println("Error: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Completed");
                    }
                });
    }
}
