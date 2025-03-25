package nufo.diss;

public abstract class Event implements Comparable<Event> {
    protected final EventSimulation eventSimulation;
    protected double executionTime;
    protected int priority;


//    public Event(Simulation simulation, double executionTime, int priority) {
//        this.simulation = simulation;
//        this.executionTime = executionTime;
//        this.priority = priority;
//    }

    public Event(EventSimulation eventSimulation, double executionTime) {
//        this(simulation, executionTime, Integer.MAX_VALUE);
        this.eventSimulation = eventSimulation;
        this.executionTime = executionTime;
        this.priority = Integer.MAX_VALUE;
    }

    public abstract void execute();

    public double getExecutionTime() {
        return executionTime;
    }

    @Override
    public int compareTo(Event o) {
        int executionTimeComparisonResult = Double.compare(executionTime, o.executionTime);
        if (executionTimeComparisonResult != 0) {
            return executionTimeComparisonResult;
        }

        return Integer.compare(priority, o.priority);
    }
}
