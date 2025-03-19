package nufo.diss;

public abstract class Event {
    double executeTime;
    int priority;
    // add reference on simulation core when will be implemented

    public Event(double executeTime, int priority) {
        this.executeTime = executeTime;
        this.priority = priority;
    }

    public Event(double executeTime) {
        this(executeTime, 0); // consider what should be default priority
    }

    public abstract void execute();
}
