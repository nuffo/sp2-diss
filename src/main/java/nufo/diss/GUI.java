package nufo.diss;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.function.Consumer;

public class GUI extends JFrame {
    private JPanel contentPane;
    private JButton startButton;
    private JLabel timeLabel;
    private JComboBox<String> timeSpeedMultiplierComboBox;
    private JButton stopButton;
    private JButton pauseButton;
    private Controller controller;

    public GUI() {
        registerListeners();
        initComponents();
        initFrame();
    }

    public Consumer<EventSimulationData> getStateConsumer() {
        return data -> SwingUtilities.invokeLater(() -> {
            updateSimulationTime(data.currentTime());
            pauseButton.setText(data.state() == MonteCarlo.SimulationState.PAUSED ? "Resume" : "Pause");
            pauseButton.setEnabled(data.state() == MonteCarlo.SimulationState.RUNNING || data.state() == MonteCarlo.SimulationState.PAUSED);
            startButton.setEnabled(data.state() == MonteCarlo.SimulationState.STOPPED);
            stopButton.setEnabled(data.state() == MonteCarlo.SimulationState.RUNNING || data.state() == MonteCarlo.SimulationState.PAUSED);
        });
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    private void registerListeners() {
        // button listeners
        startButton.addActionListener(this::startSimulation);
        stopButton.addActionListener(this::stopSimulation);
        pauseButton.addActionListener(this::pauseOrResumeSimulation);

        //combobox listeners
        timeSpeedMultiplierComboBox.addActionListener(this::setSpeedMultiplier);
    }

    private void startSimulation(ActionEvent e) {
        if (controller == null) return;

        EventSimulation.TimeMultiplier timeMultiplier = EventSimulation.TimeMultiplier.fromString((String) Objects.requireNonNull(timeSpeedMultiplierComboBox.getSelectedItem()));
        controller.startSimulation(timeMultiplier);
    }

    private void stopSimulation(ActionEvent e) {
        if (controller == null) return;
        controller.stopSimulation();
    }

    private void pauseOrResumeSimulation(ActionEvent e) {
        if (controller == null) return;

        if (Objects.equals(pauseButton.getText(), "Pause")) {
            controller.pauseSimulation();
        } else {
            controller.resumeSimulation();
        }
    }

    private void updateSimulationTime(double time) {
        int day = (int)(time / (8 * 60 * 60)) + 1;
        double timeInDay = time % (8 * 60 * 60);
        int hours = (int)(timeInDay / 3600) + 6;
        int minutes = (int)(timeInDay % 3600) / 60;
        int seconds = (int)timeInDay % 60;
        timeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    private void setSpeedMultiplier(ActionEvent e) {
        if (controller == null) return;

        EventSimulation.TimeMultiplier selectedMultiplier = EventSimulation.TimeMultiplier.fromString((String) Objects.requireNonNull(timeSpeedMultiplierComboBox.getSelectedItem()));
        controller.setSimulationTimeMultiplier(selectedMultiplier);
    }

    private void initComponents() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (EventSimulation.TimeMultiplier multiplier : EventSimulation.TimeMultiplier.values()) {
            model.addElement(EventSimulation.TimeMultiplier.valueToString(multiplier));
        }
        timeSpeedMultiplierComboBox.setModel(model);

        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        timeSpeedMultiplierComboBox.setSelectedItem(EventSimulation.TimeMultiplier.valueToString(EventSimulation.TimeMultiplier.REAL_TIME));
    }

    private void initFrame() {
        setContentPane(contentPane);
        setTitle("Udalostná simulácia stolárskej dielne - Najlepší nábytok, s.r.o");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }
}
