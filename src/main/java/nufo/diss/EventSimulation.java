package nufo.diss;

import java.util.PriorityQueue;

public abstract class EventSimulation extends SimulationCore {
    private final PriorityQueue<Event> eventCalendar;
    protected double currentTime;
    private final double maxTime;
    private TimeMultiplier timeMultiplier;
    private ExecutionMode executionMode;

    protected EventSimulation(int numberOfReplications, ExecutionMode executionMode, double maxTime) {
        super(numberOfReplications);

        this.maxTime = maxTime;
        this.eventCalendar = new PriorityQueue<>();
        this.currentTime = 0.0;
        this.timeMultiplier = TimeMultiplier.REAL_TIME;
        this.executionMode = executionMode;
    }

    public void simulate() {
        addEvent(new SystemEvent(this, currentTime));

        while (!eventCalendar.isEmpty() && currentTime < maxTime) {
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

            Event event = eventCalendar.poll();
            setCurrentTime(event.getExecutionTime());
            event.execute();

            if (executionMode == ExecutionMode.REAL_TIME) {
                notifyStateChange(StateChangeType.EVENT);
            }
        }
    }

    public void addEvent(Event event) {
        if (event.getExecutionTime() < currentTime - 1e-6) {
            throw new IllegalStateException("Simulation time must not decrease.");
        }

        eventCalendar.add(event);
    }

    public void resetEventCalendar() {
        eventCalendar.clear();
    }

    private void setCurrentTime(double time) {
        currentTime = time;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public TimeMultiplier getTimeMultiplier() {
        return timeMultiplier;
    }

    public void setTimeMultiplier(TimeMultiplier timeMultiplier) {
        this.timeMultiplier = timeMultiplier;
    }

    public void setExecutionMode(ExecutionMode executionMode) {
        if (this.executionMode == executionMode) {
            return;
        }

        this.executionMode = executionMode;

        if (executionMode == ExecutionMode.REAL_TIME) {
            addEvent(new SystemEvent(this, currentTime));
        }
    }

    public ExecutionMode getExecutionMode() {
        return executionMode;
    }

    public void experiment() {
        simulate();
    }

    protected abstract void beforeSimulation();
    protected abstract void afterSimulation();
    protected abstract void beforeExperiment();
    protected abstract void afterExperiment();

    public enum ExecutionMode {
        VIRTUAL_TIME,
        REAL_TIME
    }

    public enum TimeMultiplier {
        SLOWER_10(1/10.0),
        SLOWER_5(1/5.0),
        SLOWER_2(1/2.0),
        REAL_TIME(1.0),
        FASTER_2(2.0),
        FASTER_5(5.0),
        FASTER_10(10.0),
        FASTER_50(50.0),
        FASTER_100(100.0),
        FASTER_500(500.0),
        FASTER_1000(1000.0),
        FASTER_5000(5000.0),
        FASTER_10_000(10000.0),
        FASTER_50_000(50000.0),
        FASTER_100_000(100000.0);

        public final double value;

        TimeMultiplier(double value) {
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        public static TimeMultiplier fromString(String value) {
            return switch (value) {
                case "1/10x" -> SLOWER_10;
                case "1/5x" -> SLOWER_5;
                case "1/2x" -> SLOWER_2;
                case "1x" -> REAL_TIME;
                case "2x" -> FASTER_2;
                case "5x" -> FASTER_5;
                case "10x" -> FASTER_10;
                case "50x" -> FASTER_50;
                case "100x" -> FASTER_100;
                case "500x" -> FASTER_500;
                case "1000x" -> FASTER_1000;
                case "5000x" -> FASTER_5000;
                case "10 000x" -> FASTER_10_000;
                case "50 000x" -> FASTER_50_000;
                case "100 000x" -> FASTER_100_000;
                default -> throw new IllegalArgumentException("Unknown time multiplier: " + value);
            };
        }

        public static String valueToString(TimeMultiplier timeMultiplier) {
            return switch (timeMultiplier) {
                case SLOWER_10 -> "1/10x";
                case SLOWER_5 -> "1/5x";
                case SLOWER_2 -> "1/2x";
                case REAL_TIME -> "1x";
                case FASTER_2 -> "2x";
                case FASTER_5 -> "5x";
                case FASTER_10 -> "10x";
                case FASTER_50 -> "50x";
                case FASTER_100 -> "100x";
                case FASTER_500 -> "500x";
                case FASTER_1000 -> "1000x";
                case FASTER_5000 -> "5000x";
                case FASTER_10_000 -> "10 000x";
                case FASTER_50_000 -> "50 000x";
                case FASTER_100_000 -> "100 000x";
            };
        }
    }
}
