package nufo.diss;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Comparator;
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
    private JLabel dayLabel;
    private JTextField skipReplicationsPercentageTextField;
    private JTextField groupASizeTextField;
    private JTextField groupBSizeTextField;
    private JTextField groupCSizeTextField;
    private JList<String> workplacesList;
    private JList<String> carpentersList;
    private JLabel numberOfNewOrdersInQueue;
    private JLabel doneReplicationsLabel;
    private JLabel avgOrderWorkingTimeLabel;
    private JPanel avgOrderWorkingTimeSettlingChartPanel;
    private JPanel avgNotYetStartedWorkOrdersCountPanel;
    private JLabel avgOrderWorkingTimeReplicationLabel;
    private JLabel groupAWorkloadLabel;
    private JLabel groupBWorkloadLabel;
    private JLabel groupCWorkloadLabel;
    private JLabel avgNotYetStartedWorkOrdersReplicationLabel;
    private JList<String> carpenterWorkloadsList;
    private JPanel avgOrderWorkingTimePanel;
    private JScrollPane generalScrollPanel;
    private JPanel simulationRealTimePanel;
    private JPanel simulationReplicationPanel;
    private JScrollPane carpentersListScrollPane;
    private JScrollPane workplacesListScrollPane;
    private JSeparator generalSeparator;
    private JScrollPane carpenterWorkloadsListScrollPane;
    private Controller controller;
    private JFreeChart avgOrderWorkingTimeLineChart;
    private JFreeChart avgNotYetStartedWorkOrdersCountChart;
    private int numberOfReplications;
    private int skipReplicationsPercentage;

    public GUI() {
        registerListeners();
        initComponents();
        initFrame();
    }

    public Consumer<SimulationCore.ConsumerData> getStateConsumer() {
        return d -> SwingUtilities.invokeLater(() -> {
            switch (d.stateChangeType()) {
                case EXPERIMENT -> {
                    FurnitureSimulation.ReplicationData data = (FurnitureSimulation.ReplicationData) d.data();
                    int doneReplications = data.numberOfDoneReplications();
                    doneReplicationsLabel.setText(String.valueOf(doneReplications));
                    groupAWorkloadLabel.setText(String.format("%.2f%%", data.carpenterGroupWorkloads().get(FurnitureSimulation.Carpenter.Group.A).mean() * 100));
                    groupBWorkloadLabel.setText(String.format("%.2f%%", data.carpenterGroupWorkloads().get(FurnitureSimulation.Carpenter.Group.B).mean() * 100));
                    groupCWorkloadLabel.setText(String.format("%.2f%%", data.carpenterGroupWorkloads().get(FurnitureSimulation.Carpenter.Group.C).mean() * 100));
                    simulationReplicationPanel.setVisible(true);
                    avgOrderWorkingTimeReplicationLabel.setText(String.format("%.3fh <%.3fh, %.3fh>", data.orderWorkingTime().mean() / 3600.0, data.orderWorkingTime().confidenceIntervalLowerBound() / 3600.0, data.orderWorkingTime().confidenceIntervalUpperBound() / 3600.0));
                    avgNotYetStartedWorkOrdersReplicationLabel.setText(String.format("%.2f <%.2f, %.2f>", data.notYetStartedWorkOrders().mean(), data.notYetStartedWorkOrders().confidenceIntervalLowerBound(), data.notYetStartedWorkOrders().confidenceIntervalUpperBound()));
                    carpenterWorkloadsList.setListData(
                            data.carpenterWorkloads().entrySet()
                                    .stream()
                                    .sorted(Comparator.comparingInt(e -> e.getKey().id()))
                                    .map(e -> String.format(
                                            "Group: %s, ID: %d - %.2f%% <%.2f, %.2f>",
                                            e.getKey().group(),
                                            e.getKey().id(),
                                            e.getValue().mean() * 100,
                                            e.getValue().confidenceIntervalLowerBound() * 100,
                                            e.getValue().confidenceIntervalUpperBound() * 100)
                                    ).toArray(String[]::new)
                    );
                    if (doneReplications >= (numberOfReplications * (skipReplicationsPercentage / 100.0))) {
                        if (doneReplications % ((numberOfReplications > 1000) ? numberOfReplications / 1000 : 1) == 0 || doneReplications == numberOfReplications) {
                            XYSeriesCollection dataset = (XYSeriesCollection) avgOrderWorkingTimeLineChart.getXYPlot().getDataset();
                            dataset.getSeries("Mean").add(doneReplications, data.orderWorkingTime().mean() / 3600.0);
                            dataset.getSeries("CI Lower Bound").add(doneReplications, data.orderWorkingTime().confidenceIntervalLowerBound() / 3600.0);
                            dataset.getSeries("CI Upper Bound").add(doneReplications, data.orderWorkingTime().confidenceIntervalUpperBound() / 3600.0);

                            dataset = (XYSeriesCollection) avgNotYetStartedWorkOrdersCountChart.getXYPlot().getDataset();
                            dataset.getSeries("Mean").add(data.numberOfDoneReplications(), data.notYetStartedWorkOrders().mean());
                            dataset.getSeries("CI Lower Bound").add(data.numberOfDoneReplications(), data.notYetStartedWorkOrders().confidenceIntervalLowerBound());
                            dataset.getSeries("CI Upper Bound").add(data.numberOfDoneReplications(), data.notYetStartedWorkOrders().confidenceIntervalUpperBound());
                        }
                    }

                }
                case EVENT -> {
                    FurnitureSimulation.EventData data = (FurnitureSimulation.EventData) d.data();
                    updateSimulationTime(data.currentTime());
                    numberOfArrivedOrdersLabel.setText(String.valueOf(data.numberOfArrivedOrders()));
                    numberOfDoneOrdersLabel.setText(String.valueOf(data.numberOfDoneOrders()));
                    numberOfNewOrdersInQueue.setText(String.valueOf(data.queueSizes().get(FurnitureSimulation.Order.State.NEW)));
                    numberOfSawedOrdersInQueue.setText(String.valueOf(data.queueSizes().get(FurnitureSimulation.Order.State.SAWED)));
                    numberOfSoakedOrdersInQueue.setText(String.valueOf(data.queueSizes().get(FurnitureSimulation.Order.State.SOAKED)));
                    numberOfAssembledOrdersInQueue.setText(String.valueOf(data.queueSizes().get(FurnitureSimulation.Order.State.ASSEMBLED)));
                    workplacesList.setListData(data.workplaces());
                    carpentersList.setListData(data.carpenters());
                    avgOrderWorkingTimePanel.setVisible(!Double.isNaN(data.orderWorkingTime().getMean()));
                    avgOrderWorkingTimeLabel.setText(String.format("%.3fh", data.orderWorkingTime().getMean() / 3600.0));
                    workplacesListScrollPane.setPreferredSize(new Dimension(540,  Math.min(390, 18 * data.workplaces().length)));
                }
                case STATE -> {
                    SimulationCore.State state = (SimulationCore.State) d.data();
                    simulationStateLabel.setText(state.toString());
                    pauseButton.setText(state == SimulationCore.State.PAUSED ? "Resume" : "Pause");
                    pauseButton.setEnabled(state == SimulationCore.State.RUNNING || state == SimulationCore.State.PAUSED);
                    startButton.setEnabled(state == SimulationCore.State.STOPPED || state == SimulationCore.State.FINISHED);
                    stopButton.setEnabled(state == SimulationCore.State.RUNNING || state == SimulationCore.State.PAUSED);
                    numberOfReplicationsTextField.setEnabled(state == SimulationCore.State.STOPPED || state == SimulationCore.State.FINISHED);
                    skipReplicationsPercentageTextField.setEnabled(state == SimulationCore.State.STOPPED || state == SimulationCore.State.FINISHED);
                    groupASizeTextField.setEnabled(state == SimulationCore.State.STOPPED || state == SimulationCore.State.FINISHED);
                    groupBSizeTextField.setEnabled(state == SimulationCore.State.STOPPED || state == SimulationCore.State.FINISHED);
                    groupCSizeTextField.setEnabled(state == SimulationCore.State.STOPPED || state == SimulationCore.State.FINISHED);
                    generalScrollPanel.setVisible(state != SimulationCore.State.CREATED);
                }
            }
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
        this.numberOfReplications = numberOfReplications;

        Integer skipReplicationsPercentage = parseIntWithDialog(skipReplicationsPercentageTextField, "Please enter the percentage of points you would like skip in simulation as valid integer.");
        if (skipReplicationsPercentage == null) { return; }
        this.skipReplicationsPercentage = skipReplicationsPercentage;

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
                selectedItem.equals("Virtual") ? EventSimulation.ExecutionMode.VIRTUAL_TIME : EventSimulation.ExecutionMode.REAL_TIME,
                groupASize,
                groupBSize,
                groupCSize,
                timeMultiplier
        );

        carpentersListScrollPane.setPreferredSize(new Dimension(460,  Math.min(390, 18 * (groupASize + groupBSize + groupCSize))));
        carpenterWorkloadsListScrollPane.setPreferredSize(new Dimension(300,  Math.min(390, 18 * (groupASize + groupBSize + groupCSize))));

        resetCharts();
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

        simulationRealTimePanel.setVisible(!selectedItem.equals("Virtual"));
        generalSeparator.setVisible(!selectedItem.equals("Virtual"));
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

        generalScrollPanel.setVisible(false);
        simulationReplicationPanel.setVisible(false);
        avgOrderWorkingTimePanel.setVisible(false);
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

    private void createUIComponents() {
        avgOrderWorkingTimeLineChart = createChart("Average order working time","Replication", "Time [hours]");
        avgOrderWorkingTimeSettlingChartPanel = new ChartPanel(avgOrderWorkingTimeLineChart);
        avgOrderWorkingTimeSettlingChartPanel.setPreferredSize(new Dimension(500, 390));

        avgNotYetStartedWorkOrdersCountChart = createChart("Average count of not yet started work orders", "Replication", "Count");
        avgNotYetStartedWorkOrdersCountPanel = new ChartPanel(avgNotYetStartedWorkOrdersCountChart);
        avgNotYetStartedWorkOrdersCountPanel.setPreferredSize(new Dimension(500, 390));
    }

    private JFreeChart createChart(String title, String xAxisLabel, String yAxisLabel) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries mean = new XYSeries("Mean");
        XYSeries ciLowerBound = new XYSeries("CI Lower Bound");
        XYSeries ciUpperBound = new XYSeries("CI Upper Bound");
        dataset.addSeries(mean);
        dataset.addSeries(ciLowerBound);
        dataset.addSeries(ciUpperBound);

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                xAxisLabel,
                yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );

        ((NumberAxis) chart.getXYPlot().getRangeAxis()).setAutoRangeIncludesZero(false);

        return chart;
    }

    private void resetCharts() {
        for (Object s : ((XYSeriesCollection) avgOrderWorkingTimeLineChart.getXYPlot().getDataset()).getSeries()) {
            ((XYSeries) s).clear();
        }

        for (Object s : ((XYSeriesCollection) avgNotYetStartedWorkOrdersCountChart.getXYPlot().getDataset()).getSeries()) {
            ((XYSeries) s).clear();
        }
    }
}
