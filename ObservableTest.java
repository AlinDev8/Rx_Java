import org.junit.jupiter.api.Test;
import customrx.core.Observable;
import customrx.core.Observer;
import customrx.schedulers.ComputationScheduler;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class ObservableTest {

    @Test
    void testMapAndFilter() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Observable.<Integer>create(emitter -> {
                    emitter.onNext(1);
                    emitter.onNext(2);
                    emitter.onNext(3);
                    emitter.onComplete();
                })
                .map(i -> i * 2)
                .filter(i -> i > 2)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        System.out.println("Next: " + item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Error occurred: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        latch.await();
    }

    @Test
    void testFlatMap() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Observable.<Integer>create(emitter -> {
                    emitter.onNext(1);
                    emitter.onNext(2);
                    emitter.onComplete();
                })
                .flatMap(i -> Observable.<Integer>create(em -> {
                    em.onNext(i);
                    em.onNext(i * 10);
                    em.onComplete();
                }))
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        System.out.println("FlatMapped: " + item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Error occurred: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        latch.await();
    }

    @Test
    void testSchedulers() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Observable.<String>create(emitter -> {
                    emitter.onNext("Hello");
                    emitter.onComplete();
                })
                .subscribeOn(new ComputationScheduler())
                .observeOn(new ComputationScheduler())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String item) {
                        System.out.println("Received: " + item + " on thread: " + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable t) {}

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }
                });

        latch.await();
    }
}
