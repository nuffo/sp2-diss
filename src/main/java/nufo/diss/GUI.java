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
    private JTextField numberOfReplicationsTextField;
    private JLabel simulationStateLabel;
    private JLabel numberOfArrivedOrdersLabel;
    private JLabel numberOfDoneOrdersLabel;
    private JLabel numberOfSawedOrdersInQueue;
    private JLabel numberOfSoakedOrdersInQueue;
    private JLabel numberOfAssembledOrdersInQueue;
    private JLabel numberOfSawedOrders;
    private JLabel numberOfSoakedOrders;
    private JLabel numberOfAssembledOrders;
    private JLabel numberOfFittingsInstalledOrders;
    private JLabel dayLabel;
    private JTextField skipReplicationsPercentageTextField;
    private JTextField groupASizeTextField;
    private JTextField groupBSizeTextField;
    private JTextField groupCSizeTextField;
    private JList<String> workplacesList;
    private JList<String> carpentersList;
    private JLabel numberOfNewOrdersInQueue;
    private Controller controller;

    public GUI() {
        registerListeners();
        initComponents();
        initFrame();
    }

    public Consumer<? super SimulationData> getStateConsumer() {
        return d -> SwingUtilities.invokeLater(() -> {
            FurnitureSimulation.FurnitureSimulationData data = (FurnitureSimulation.FurnitureSimulationData) d;
            updateSimulationTime(data.getCurrentTime());
            MonteCarlo.State state = data.getState();
            simulationStateLabel.setText(state.toString());
            numberOfArrivedOrdersLabel.setText(String.valueOf(data.getNumberOfArrivedOrders()));
            numberOfDoneOrdersLabel.setText(String.valueOf(data.getNumberOfDoneOrders()));
            numberOfNewOrdersInQueue.setText(String.valueOf(data.getNumberOfNewOrdersInQueue()));
            numberOfSawedOrdersInQueue.setText(String.valueOf(data.getNumberOfSawedOrdersInQueue()));
            numberOfSoakedOrdersInQueue.setText(String.valueOf(data.getNumberOfSoakedOrdersInQueue()));
            numberOfAssembledOrdersInQueue.setText(String.valueOf(data.getNumberOfAssembledOrdersInQueue()));
            pauseButton.setText(state == MonteCarlo.State.PAUSED ? "Resume" : "Pause");
            pauseButton.setEnabled(state == MonteCarlo.State.RUNNING || state == MonteCarlo.State.PAUSED);
            startButton.setEnabled(state == MonteCarlo.State.STOPPED || state == MonteCarlo.State.FINISHED);
            stopButton.setEnabled(state == MonteCarlo.State.RUNNING || state == MonteCarlo.State.PAUSED);
            numberOfReplicationsTextField.setEnabled(state == MonteCarlo.State.STOPPED || state == MonteCarlo.State.FINISHED);
            skipReplicationsPercentageTextField.setEnabled(state == MonteCarlo.State.STOPPED || state == MonteCarlo.State.FINISHED);
            groupASizeTextField.setEnabled(state == MonteCarlo.State.STOPPED || state == MonteCarlo.State.FINISHED);
            groupBSizeTextField.setEnabled(state == MonteCarlo.State.STOPPED || state == MonteCarlo.State.FINISHED);
            groupCSizeTextField.setEnabled(state == MonteCarlo.State.STOPPED || state == MonteCarlo.State.FINISHED);
            workplacesList.setListData(data.getWorkplaces());
            carpentersList.setListData(data.getCarpenters());
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

        Integer numberOfReplications = parseIntWithDialog(numberOfReplicationsTextField, "Please enter the number of replications you would like to simulate as valid integer.");
        if (numberOfReplications == null) { return; }

        Integer skipReplicationsPercentage = parseIntWithDialog(skipReplicationsPercentageTextField, "Please enter the percentage of points you would like skip in simulation as valid integer.");
        if (skipReplicationsPercentage == null) { return; }

        Integer groupASize = parseIntWithDialog(groupASizeTextField, "Please enter the group A size as valid integer.");
        if (groupASize == null) { return; }

        Integer groupBSize = parseIntWithDialog(groupBSizeTextField, "Please enter the group B size as valid integer.");
        if (groupBSize == null) { return; }

        Integer groupCSize = parseIntWithDialog(groupCSizeTextField, "Please enter the group C size as valid integer.");
        if (groupCSize == null) { return; }

        String selectedItem = (String) Objects.requireNonNull(timeSpeedMultiplierComboBox.getSelectedItem());

        EventSimulation.TimeMultiplier timeMultiplier = selectedItem.equals("Virtual") ? EventSimulation.TimeMultiplier.REAL_TIME : EventSimulation.TimeMultiplier.fromString(selectedItem);
        controller.startSimulation(
                numberOfReplications,
                skipReplicationsPercentage,
                selectedItem.equals("Virtual") ? EventSimulation.ExecutionMode.VIRTUAL_TIME : EventSimulation.ExecutionMode.REAL_TIME,
                groupASize,
                groupBSize,
                groupCSize,
                timeMultiplier
        );
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
        dayLabel.setText(String.valueOf(day));
    }

    private void setSpeedMultiplier(ActionEvent e) {
        if (controller == null) return;

        String selectedItem = (String) Objects.requireNonNull(timeSpeedMultiplierComboBox.getSelectedItem());

        if (!selectedItem.equals("Virtual")) {
            EventSimulation.TimeMultiplier selectedMultiplier = EventSimulation.TimeMultiplier.fromString(selectedItem);
            controller.setSimulationExecutionMode(EventSimulation.ExecutionMode.REAL_TIME);
            controller.setSimulationTimeMultiplier(selectedMultiplier);
        } else {
            controller.setSimulationExecutionMode(EventSimulation.ExecutionMode.VIRTUAL_TIME);
        }
    }

    private void initComponents() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (EventSimulation.TimeMultiplier multiplier : EventSimulation.TimeMultiplier.values()) {
            model.addElement(EventSimulation.TimeMultiplier.valueToString(multiplier));
        }
        model.addElement("Virtual");
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

    private void showWarning(String message) { JOptionPane.showMessageDialog(null, message, null, JOptionPane.WARNING_MESSAGE); }

    private Integer parseIntWithDialog(JTextField textField, String errorMessage) {
        try {
            return Integer.parseInt(textField.getText());
        } catch (NumberFormatException e) {
            showWarning(errorMessage);
            textField.requestFocus();
            return null;
        }
    }
}
