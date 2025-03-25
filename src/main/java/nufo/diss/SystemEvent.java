package nufo.diss;

import java.util.logging.Logger;

public class SystemEvent extends Event {
    private static final Logger logger = Logger.getLogger(SystemEvent.class.getName());

    public SystemEvent(EventSimulation eventSimulation, double executionTime) {
        super(eventSimulation, executionTime);
    }

    @Override
    public void execute() {
        try {
            Thread.sleep((long) (1000 / eventSimulation.getTimeMultiplier().getValue()));
        } catch (InterruptedException e) {
            logger.warning("Thread was interrupted: " + e.getMessage());
        }

        eventSimulation.addEvent(new SystemEvent(eventSimulation, executionTime + 1));
    }
}
