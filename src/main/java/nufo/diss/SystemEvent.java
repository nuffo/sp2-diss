package nufo.diss;

import java.util.logging.Logger;

public class SystemEvent extends Event {
    private static final Logger logger = Logger.getLogger(SystemEvent.class.getName());

    public SystemEvent(EventSimulation eventSimulation, double executionTime) {
        super(eventSimulation, executionTime);
    }

    @Override
    public void execute() {
        if (simulation.getExecutionMode() == EventSimulation.ExecutionMode.REAL_TIME) {
            long targetDelayNanos = (long) (1_000_000_000 / simulation.getTimeMultiplier().getValue());

            long startTime = System.nanoTime();
            while (System.nanoTime() - startTime < targetDelayNanos) {
                Thread.onSpinWait();
            }

            simulation.addEvent(new SystemEvent(simulation, executionTime + 1));
        }
    }
}
