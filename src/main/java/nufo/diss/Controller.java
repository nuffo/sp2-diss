package nufo.diss;

public class Controller {
    private EventSimulation simulation;
    private final GUI gui;

    public Controller(GUI gui) {
        this.gui = gui;
    }

    public void startSimulation(int numberOfReplications, EventSimulation.ExecutionMode executionMode, int groupASize, int groupBSize, int groupCSize, EventSimulation.TimeMultiplier timeMultiplier) {
        simulation = new FurnitureSimulation(numberOfReplications, executionMode, 249 * 8 * 60 * 60 - 1, groupASize, groupBSize, groupCSize);
        simulation.setTimeMultiplier(timeMultiplier);
        simulation.setConsumer(gui.getStateConsumer());

        Thread simulationThread = new Thread(() -> simulation.run());
        simulationThread.start();
    }

    public void stopSimulation() {
        simulation.stop();
    }

    public void pauseSimulation() {
        simulation.pause();
    }

    public void resumeSimulation() {
        simulation.resume();
    }

    public void setSimulationTimeMultiplier(EventSimulation.TimeMultiplier multiplier) {
        if (simulation == null) return;

        simulation.setTimeMultiplier(multiplier);
    }

    public void setSimulationExecutionMode(EventSimulation.ExecutionMode mode) {
        if (simulation == null) return;
        simulation.setExecutionMode(mode);
    }
}
