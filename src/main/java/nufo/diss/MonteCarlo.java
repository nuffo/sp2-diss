package nufo.diss;

import java.util.function.Consumer;

public abstract class MonteCarlo {
    protected SimulationState simulationState;
    protected int numberOfReplications;
    protected int actualReplication;
//    private final int totalPoints;
    private final int skipReplicationsPercentage;
    protected final Object lock = new Object();
    protected Consumer<EventSimulationData> dataConsumer;

    MonteCarlo(int numberOfReplications, int skipReplicationsPercentage) {
        this.simulationState = SimulationState.CREATED;
        this.numberOfReplications = numberOfReplications;
        this.actualReplication = 1;
//        this.totalPoints = totalPoints;
        this.skipReplicationsPercentage = skipReplicationsPercentage;
    }

    public void run() {
        if (simulationState == SimulationState.RUNNING) {
            throw new IllegalStateException("Simulation already running");
        }

        simulationState = SimulationState.RUNNING;

//        double sum = 0;
//        int step = (numberOfReplications > totalPoints) ? numberOfReplications / totalPoints : 1;

        beforeSimulation();

        for (actualReplication = 1; actualReplication <= numberOfReplications; actualReplication++) {
            if (simulationState == SimulationState.STOPPED) {
                break;
            }

            beforeExperiment();

            this.experiment();

            if (actualReplication > (numberOfReplications * (skipReplicationsPercentage / 100.0))) {
//                replicationCallback.accept(actualReplication, sum / actualReplication);
            }

            afterExperiment();
        }

        afterSimulation();

        simulationState = SimulationState.FINISHED;
    }

    public void stop() {
        if (simulationState == SimulationState.STOPPED) {
            throw new IllegalStateException("Simulation already stopped.");
        }
        simulationState = SimulationState.STOPPED;
        notifyStateChange();
    }

    public void pause() {
        if (simulationState == SimulationState.PAUSED) {
            throw new IllegalStateException("Simulation already paused.");
        }
        simulationState = SimulationState.PAUSED;
        notifyStateChange();
    }

    public void resume() {
        synchronized (lock) {
            simulationState = SimulationState.RUNNING;
            lock.notifyAll();
            notifyStateChange();
        }
    }

    public void setDataConsumer(Consumer<EventSimulationData> consumer) {
        this.dataConsumer = consumer;
    }

    protected abstract void notifyStateChange();

    public abstract void experiment();

    protected abstract void beforeSimulation();
    protected abstract void afterSimulation();
    protected abstract void beforeExperiment();
    protected abstract void afterExperiment();

    public enum SimulationState {
        CREATED,
        RUNNING,
        STOPPED,
        PAUSED,
        FINISHED
    }
}
