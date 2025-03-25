package nufo.diss;

import javax.swing.*;

public class Controller {
    private EventSimulation simulation;
    private final GUI gui;

    public Controller(GUI gui) {
        this.gui = gui;
    }

    public void startSimulation(EventSimulation.TimeMultiplier timeMultiplier) {
        simulation = new Station(1, 5, EventSimulation.ExecutionMode.REAL_TIME, 12 * 60 * 60);
        simulation.setTimeMultiplier(timeMultiplier);
        simulation.setDataConsumer(gui.getStateConsumer());

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                simulation.run();
                return null;
            }

            @Override
            protected void done() {
                System.out.println("stopped");
            }
        };
        worker.execute();
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

    public EventSimulation.TimeMultiplier getSimulationTimeMultiplier() {
        return simulation.getTimeMultiplier();
    }
}
