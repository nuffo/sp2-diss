package nufo.diss;

public abstract class Event implements Comparable<Event> {
    protected final EventSimulation simulation;
    protected double executionTime;

    public Event(EventSimulation simulation, double executionTime) {
        this.simulation = simulation;
        this.executionTime = executionTime;
    }

    public abstract void execute();

    public double getExecutionTime() {
        return executionTime;
    }

    @Override
    public int compareTo(Event o) {
        return Double.compare(executionTime, o.executionTime);
    }
}
