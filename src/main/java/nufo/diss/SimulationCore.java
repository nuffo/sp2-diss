package nufo.diss;

import java.util.function.Consumer;

public abstract class SimulationCore {
    protected State state;
    protected int numberOfReplications;
    protected int doneReplications;
    protected final Object lock = new Object();
    protected Consumer<ConsumerData> consumer;

    SimulationCore(int numberOfReplications) {
        this.state = State.CREATED;
        this.numberOfReplications = numberOfReplications;
        this.doneReplications = 0;
    }

    public void run() {
        if (state == State.RUNNING) {
            throw new IllegalStateException("Simulation already running");
        }

        beforeSimulation();

        setState(State.RUNNING);

        for (int i = 1; i <= numberOfReplications; i++) {
            if (state == State.STOPPED) {
                break;
            }

            synchronized (lock) {
                while (state == State.PAUSED) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            beforeExperiment();

            this.experiment();

            if (state != State.STOPPED) {
                doneReplications++;
                afterExperiment();
                notifyStateChange(StateChangeType.EXPERIMENT);
            }
        }

        afterSimulation();

        setState(State.FINISHED);
    }

    public void stop() {
        if (state == State.STOPPED) {
            throw new IllegalStateException("Simulation already stopped.");
        }
        setState(State.STOPPED);
    }

    public void pause() {
        if (state == State.PAUSED) {
            throw new IllegalStateException("Simulation already paused.");
        }
        setState(State.PAUSED);
    }

    public void resume() {
        synchronized (lock) {
            setState(State.RUNNING);
            lock.notifyAll();
        }
    }

    public void setConsumer(Consumer<ConsumerData> consumer) {
        this.consumer = consumer;
    }

    private void setState(State state) {
        this.state = state;
        notifyStateChange(StateChangeType.STATE);
    }

    public State getState() {
        return state;
    }

    public int getDoneReplications() {
        return doneReplications;
    }

    protected abstract void notifyStateChange(StateChangeType type);

    public abstract void experiment();

    protected abstract void beforeSimulation();
    protected abstract void afterSimulation();
    protected abstract void beforeExperiment();
    protected abstract void afterExperiment();

    public enum State {
        CREATED,
        RUNNING,
        STOPPED,
        PAUSED,
        FINISHED
    }

    public enum StateChangeType {
        EXPERIMENT,
        EVENT,
        STATE
    }

    public record ConsumerData(StateChangeType stateChangeType, Object data) {}
}
