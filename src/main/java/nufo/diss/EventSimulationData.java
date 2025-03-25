package nufo.diss;

public record EventSimulationData(
        double currentTime,
        MonteCarlo.SimulationState state
) {
    public String getFormattedCurrentTime() {
        int hours = (int) (currentTime / 3600);
        int minutes = (int) ((currentTime % 3600) / 60);
        int seconds = (int) (currentTime % 60);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
