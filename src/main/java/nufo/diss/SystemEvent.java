package nufo.diss;

public class SystemEvent extends Event {
    public SystemEvent(EventSimulation eventSimulation, double executionTime) {
        super(eventSimulation, executionTime);
    }

    @Override
    public void execute() {
        if (simulation.getExecutionMode() == EventSimulation.TimeMode.REAL_TIME) {
            long targetDelayNanos = (long) (1_000_000_000 / simulation.getTimeMultiplier().getValue());

            long startTime = System.nanoTime();
            while (System.nanoTime() - startTime < targetDelayNanos) {
                Thread.onSpinWait();
            }

            simulation.addEvent(new SystemEvent(simulation, executionTime + 1));
        }
    }
}
