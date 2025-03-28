package nufo.diss;

public class SimulationData {
    private final double currentTime;
    private final MonteCarlo.State state;

    SimulationData(
            double currentTime,
            MonteCarlo.State state
    ) {
        this.currentTime = currentTime;
        this.state = state;
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public MonteCarlo.State getState() {
        return state;
    }
}
