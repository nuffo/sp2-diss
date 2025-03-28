package nufo.diss;

import nufo.diss.generators.ExponentialGenerator;

import java.util.LinkedList;
import java.util.Queue;

public class Station extends EventSimulation {
    private ExponentialGenerator customerEntryGenerator;
    private ExponentialGenerator serviceTimeGenerator;
    private Queue<StationCustomer> waitingCustomersQueue;
    private boolean service;
    private Statistics averageCustomerWaitingTimeInQueue;
    private Statistics averageSizeOfWaitingCustomersQueue;

    protected Station(int numberOfReplications, int skipReplicationsPercentage, ExecutionMode executionMode, double endTime) {
        super(numberOfReplications, skipReplicationsPercentage, executionMode, endTime);

    }

    @Override
    protected void notifyStateChange() {
        if (dataConsumer != null) {
            dataConsumer.accept(new SimulationData(
                    currentTime,
                    state
            ));
        }
    }

    @Override
    protected void beforeSimulation() {
        customerEntryGenerator = new ExponentialGenerator(12.0 / 3600.0);
        serviceTimeGenerator = new ExponentialGenerator(1/ (4.0 * 60));
        waitingCustomersQueue = new LinkedList<>();
        service = false;

        averageCustomerWaitingTimeInQueue = new Statistics();
        averageSizeOfWaitingCustomersQueue = new Statistics();

        addEvent(new CustomerArrivalEvent(this, customerEntryGenerator.nextDouble()));
    }

    @Override
    protected void afterSimulation() {

    }

    @Override
    protected void beforeExperiment() {

    }

    @Override
    protected void afterExperiment() {

    }

    public double getNextCustomerArrivalTime() {
       return currentTime + customerEntryGenerator.nextDouble();
    }

    public double getNextServiceTime() {
        return currentTime + serviceTimeGenerator.nextDouble();
    }

    public boolean isService() {
        return service;
    }

    public void castService() {
        if (service) {
            throw new IllegalStateException("Service is already running.");
        }
        this.service = true;
    }

    public void freeService() {
        if (!service) {
            throw new IllegalStateException("Service is not running.");
        }
        this.service = false;
    }

    public boolean addCustomerToQueue(StationCustomer customer) {
        return waitingCustomersQueue.add(customer);
    }

    public StationCustomer serveAnotherCustomer() {
        if (someoneIsWaitingForService()) {
            return waitingCustomersQueue.poll();
        } else {
            throw new IllegalStateException("Queue is empty.");
        }
    }

    public int getWaitingCustomersQueueSize() {
        return waitingCustomersQueue.size();
    }

    public boolean someoneIsWaitingForService() {
        return !waitingCustomersQueue.isEmpty();
    }

    public static class StationCustomer {
        private final double arrivalTime;

        public StationCustomer(double arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        public double getArrivalTime() {
            return arrivalTime;
        }
    }

    private abstract static class StationEvent extends Event {
        private StationCustomer customer;

        public StationEvent(Station simulation, double executionTime, StationCustomer customer) {
            super(simulation, executionTime);
        }

        public StationEvent(Station simulation, double executionTime) {
            super(simulation, executionTime);
            customer = null;
        }
    }

    private static class CustomerArrivalEvent extends StationEvent {
        public CustomerArrivalEvent(Station simulation, double executionTime) {
            super(simulation, executionTime);
        }

        @Override
        public void execute() {
            System.out.println("Customer Arrival Event");
            Station station = (Station) simulation;

            StationCustomer customer = new StationCustomer(simulation.getCurrentTime());

            if (station.isService()) {
                station.addCustomerToQueue(customer);
            } else {
                station.castService();
                station.addEvent(new ServiceStartEvent(station, station.getCurrentTime(), customer));
            }

            station.addEvent(new CustomerArrivalEvent(station, station.getNextCustomerArrivalTime()));

            station.averageSizeOfWaitingCustomersQueue.addValue(station.getWaitingCustomersQueueSize());
        }
    }

    private static class ServiceStartEvent extends StationEvent {
        private final StationCustomer customer;
        public ServiceStartEvent(Station simulation, double executionTime, StationCustomer customer) {
            super(simulation, executionTime);

            this.customer = customer;
        }

        @Override
        public void execute() {
            System.out.println("Service Start Event");
            Station station = (Station) simulation;

            station.addEvent(new ServiceEndEvent(station, station.getNextServiceTime()));

            station.averageCustomerWaitingTimeInQueue.addValue(station.getCurrentTime() - customer.getArrivalTime());
        }
    }

    private static class ServiceEndEvent extends StationEvent {
        public ServiceEndEvent(Station simulation, double executionTime) {
            super(simulation, executionTime);
        }

        @Override
        public void execute() {
            System.out.println("Service End Event");
            Station station = (Station) simulation;

            if (station.someoneIsWaitingForService()) {
                station.addEvent(new ServiceStartEvent(station, station.getCurrentTime(), station.serveAnotherCustomer()));
            } else {
                station.freeService();
            }
        }
    }
}
