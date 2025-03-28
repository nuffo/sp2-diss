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
            try {
                Thread.sleep((long) (1000 / simulation.getTimeMultiplier().getValue()));
            } catch (InterruptedException e) {
                logger.warning("Thread was interrupted: " + e.getMessage());
            }

            simulation.addEvent(new SystemEvent(simulation, executionTime + 1));
        }
    }
}
