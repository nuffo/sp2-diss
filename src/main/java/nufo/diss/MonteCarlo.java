package nufo.diss;

import java.util.function.Consumer;

public abstract class MonteCarlo {
    protected State state;
    protected int numberOfReplications;
    protected int actualReplication;
//    private final int totalPoints;
    private final int skipReplicationsPercentage;
    protected final Object lock = new Object();
    protected Consumer<? super SimulationData> dataConsumer;

    MonteCarlo(int numberOfReplications, int skipReplicationsPercentage) {
        this.state = State.CREATED;
        this.numberOfReplications = numberOfReplications;
        this.actualReplication = 1;
//        this.totalPoints = totalPoints;
        this.skipReplicationsPercentage = skipReplicationsPercentage;
    }

    public void run() {
        if (state == State.RUNNING) {
            throw new IllegalStateException("Simulation already running");
        }

        state = State.RUNNING;

//        double sum = 0;
//        int step = (numberOfReplications > totalPoints) ? numberOfReplications / totalPoints : 1;

        beforeSimulation();

        for (actualReplication = 1; actualReplication <= numberOfReplications; actualReplication++) {
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

//            if (actualReplication > (numberOfReplications * (skipReplicationsPercentage / 100.0))) {
//                replicationCallback.accept(actualReplication, sum / actualReplication);
//            }

            afterExperiment();
        }

        afterSimulation();

        state = State.FINISHED;
        notifyStateChange();
    }

    public void stop() {
        if (state == State.STOPPED) {
            throw new IllegalStateException("Simulation already stopped.");
        }
        state = State.STOPPED;
        notifyStateChange();
    }

    public void pause() {
        if (state == State.PAUSED) {
            throw new IllegalStateException("Simulation already paused.");
        }
        state = State.PAUSED;
        notifyStateChange();
    }

    public void resume() {
        synchronized (lock) {
            state = State.RUNNING;
            lock.notifyAll();
            notifyStateChange();
        }
    }

    public void setDataConsumer(Consumer<? super SimulationData> consumer) {
        this.dataConsumer = consumer;
    }

    protected abstract void notifyStateChange();

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
}
