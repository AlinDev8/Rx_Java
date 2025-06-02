package customrx.schedulers;

public interface Scheduler {
    void execute(Runnable task);
}